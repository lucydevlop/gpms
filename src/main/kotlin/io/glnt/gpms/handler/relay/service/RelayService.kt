package io.glnt.gpms.handler.relay.service

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JSONUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.model.reqPayData
import io.glnt.gpms.handler.facility.model.reqPaymentResponse
import io.glnt.gpms.handler.facility.model.reqPaymentResult
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.model.FacilitiesFailureAlarm
import io.glnt.gpms.handler.relay.model.FacilitiesStatusNoti
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.handler.tmap.model.reqTmapFacilitiesFailureAlarm
import io.glnt.gpms.handler.tmap.model.reqTmapFacilitiesStatusNoti
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.entity.ParkAlarmSetting
import io.glnt.gpms.model.enums.checkUseStatus
import io.glnt.gpms.model.repository.FailureRepository
import io.glnt.gpms.model.repository.ParkAlarmSetttingRepository
import io.glnt.gpms.model.repository.ParkFacilityRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class RelayService {
    companion object : KLogging()

    lateinit var parkAlarmSetting: ParkAlarmSetting

//    lateinit var failureList: ArrayList<Failure>

    @Autowired
    private lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var facilityService: FacilityService

    @Autowired
    private lateinit var inoutService: InoutService

    @Autowired
    private lateinit var parkAlarmSettingRepository: ParkAlarmSetttingRepository

    @Autowired
    private lateinit var failureRepository: FailureRepository

    @Autowired
    private lateinit var parkingFacilityRepository: ParkFacilityRepository

    fun fetchParkAlarmSetting(parkId: String) {
        parkAlarmSettingRepository.findBySiteid(parkId)?.let { it ->
            parkAlarmSetting = it
        }
    }

    fun facilitiesHealthCheck(request: reqRelayHealthCheck) {
        logger.info { "facilitiesHealthCheck $request" }
        try {
            if (parkinglotService.parkSite.tmapSend == "ON")
                tmapSendService.sendHealthCheckRequest(request, "")

            request.facilitiesList.forEach { facility ->
                facilityService.updateHealthCheck(facility.facilitiesId, facility.healthStatus!!)
            }

            if (parkAlarmSetting.payAlarm == checkUseStatus.Y && parkAlarmSetting.payLimitTime!! > 0) {
                paymentHealthCheck()
            }

        }catch (e: CustomException){
            logger.error { "facilitiesHealthCheck failed ${e.message}" }
        }

    }

    fun statusNoti(request: reqRelayHealthCheck) {
        logger.info { "statusNoti $request" }
        try {
            val result = ArrayList<FacilitiesStatusNoti>()

            request.facilitiesList.forEach { facility ->
                val data = facilityService.updateStatusCheck(facility.facilitiesId, facility.healthStatus!!)
                if (data != null) {
                    result.add(FacilitiesStatusNoti(facilitiesId = facility.facilitiesId, STATUS = facility.healthStatus!!))
                    // close 상태 수신 시 error 상태 check
                    if (facility.healthStatus!! == "CLOSE") {
                        saveFailure(
                            Failure(
                                sn = null,
                                issueDateTime = LocalDateTime.now(),
//                                        expireDateTime = LocalDateTime.now(),
                                facilitiesId = facility.facilitiesId,
                                fName = parkinglotService.getFacility(facility.facilitiesId)!!.fname,
                                failureCode = "crossingGateBarDamageDoubt",
                                failureType = "NORMAL"
                            )
                        )
                    }
                }
            }

            if (parkinglotService.parkSite.tmapSend == "ON" && result.isNotEmpty())
                tmapSendService.sendFacilitiesStatusNoti(reqTmapFacilitiesStatusNoti(facilitiesList = result), null)

        } catch (e: CustomException){
            logger.error { "statusNoti failed ${e.message}" }
        }
    }

    fun failureAlarm(request: reqRelayHealthCheck) {
        logger.info { "failureAlarm $request" }
        try {
            request.facilitiesList.forEach { failure ->
                parkinglotService.getFacility(facilityId = failure.facilitiesId)?.let { facility ->
                    if (failure.failureAlarm == "noResponse") {
                        // ping fail -> noResponse
                        facilityService.updateHealthCheck(failure.facilitiesId, failure.failureAlarm!!)
                    } else if (failure.failureAlarm == "crossingGateBarDamageDoubt") {
                        // 차단기
                        saveFailure(
                            Failure(sn = null,
                                issueDateTime = LocalDateTime.now(),
//                                        expireDateTime = LocalDateTime.now(),
                                facilitiesId = failure.facilitiesId,
                                fName = facility.fname,
                                failureCode = failure.failureAlarm,
                                failureType = failure.failureAlarm)
                        )
                    } else {
                        // 정산기
                    }
                    if (parkinglotService.parkSite.tmapSend == "ON")
                        tmapSendService.sendFacilitiesFailureAlarm(FacilitiesFailureAlarm(facilitiesId = failure.facilitiesId, failureAlarm = failure.failureAlarm!!), null)
                }
            }

        } catch (e: CustomException){
            logger.error { "failureAlarm failed ${e.message}" }
        }
    }

    fun paymentHealthCheck() {
        logger.info { "paymentHealthCheck" }
        try {
            val result = ArrayList<FacilitiesFailureAlarm>()
            parkinglotService.getFacilityByCategory("PAYSTATION")?.let { facilities ->
                facilities.forEach { facility ->
                    inoutService.lastSettleData(facility.facilitiesId!!)?.let { out ->
                        //정산기 마지막 페이 시간 체크
                        if (DateUtil.diffHours(
                                DateUtil.stringToLocalDateTime(out.approveDatetime!!, "yyyy-MM-dd HH:mm:ss"),
                                LocalDateTime.now()) > parkAlarmSetting.payLimitTime!!) {
                            if (parkinglotService.parkSite.tmapSend == "ON" && result.isNotEmpty())
                                tmapSendService.sendFacilitiesFailureAlarm(FacilitiesFailureAlarm(facilitiesId = facility.facilitiesId!!, failureAlarm = "dailyUnAdjustment"), null)
                            saveFailure(
                                Failure(sn = null,
                                    issueDateTime = LocalDateTime.now(),
//                                    expireDateTime = LocalDateTime.now(),
                                    facilitiesId = facility.facilitiesId,
                                    fName = facility.fname,
                                    failureCode = "dailyUnAdjustment",
                                    failureType = "dailyUnAdjustment")
                            )
                        } else {
                            saveFailure(
                                Failure(sn = null,
                                        issueDateTime = LocalDateTime.now(),
//                                        expireDateTime = LocalDateTime.now(),
                                        facilitiesId = facility.facilitiesId,
                                        fName = facility.fname,
                                        failureCode = "dailyUnAdjustment",
                                        failureType = "NORMAL")
                            )
                        }
                    } ?: run{

                    }
                }
            }
        }catch  (e: CustomException){
            logger.error { "paymentHealthCheck failed ${e.message}" }
        }
    }

    fun saveFailure(request: Failure) {
        logger.info { "saveFailure $request" }
        try {
            if (request.failureType == "NORMAL") {
                failureRepository.findTopByFacilitiesIdAndFailureCodeAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(
                    request.facilitiesId!!,
                    request.failureCode!!
                )?.let {
                    it.expireDateTime = LocalDateTime.now()
                    failureRepository.save(it)
                }
            } else {
                failureRepository.findTopByFacilitiesIdAndFailureCodeAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(
                    request.facilitiesId!!,
                    request.failureCode!!
                )?.let { it ->
                    it.failureFlag = it.failureFlag!! + 1
                    it.expireDateTime = null
                    failureRepository.save(it)
                } ?: run {
                    failureRepository.save(request)
                }
            }
        }catch (e: CustomException){
            logger.error { "saveFailure failed ${e.message}" }
        }
    }

    fun paymentResult(request: reqApiTmapCommon, facilityId: String){
        request.contents = JSONUtil.getJsObject(request.contents)
        val contents = readValue(request.contents.toString(), reqPaymentResult::class.java)
        facilityService.sendPaystation(
            reqPaymentResponse(
                chargingId = contents.transactionId,
                vehicleNumber = contents.vehicleNumber
            ),
            gate = parkingFacilityRepository.findByFacilitiesId(facilityId)!!.gateId,
            requestId = request.requestId!!,
            type = "paymentResponse"
        )
        val result = inoutService.paymentResult(contents, request.requestId!!, parkingFacilityRepository.findByFacilitiesId(facilityId)!!.gateId)
    }

    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
        val data = JSONUtil.getJSONObject(any)
        val factory = JsonFactory()
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        return jacksonObjectMapper().readValue(data.toString(), valueType)
    }
}