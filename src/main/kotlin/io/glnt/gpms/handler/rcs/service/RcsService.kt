package io.glnt.gpms.handler.rcs.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.RcsClient
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
//import io.glnt.gpms.handler.corp.service.CorpService
import io.glnt.gpms.handler.dashboard.admin.service.singleTimer
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.service.InoutService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.rcs.model.*
import io.glnt.gpms.common.api.ResetClient
import io.glnt.gpms.service.RelayService
import io.glnt.gpms.model.dto.FacilityDTO
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ExternalSvrType
import io.glnt.gpms.service.CorpService
import io.glnt.gpms.service.ParkSiteInfoService
import io.reactivex.Observable
import mu.KLogging
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RcsService(
    private var facilityService: FacilityService,
    private var parkinglotService: ParkinglotService,
    private var inoutService: InoutService,
    private var restAPIManager: RestAPIManagerUtil,
    private var productService: ProductService,
    private var discountService: DiscountService,
    private var corpService: CorpService,
    private var parkSiteInfoService: ParkSiteInfoService,
    private var rcsClient: RcsClient,
    private var resetClient: ResetClient
) {
    companion object : KLogging()

    @Autowired
    lateinit var relayService: RelayService

    @Value("\${glnt.url}")
    lateinit var glntUrl: String

    @Value("\${adtcaps.url}")
    lateinit var adtcapsUrl: String

    fun asyncParkinglot(): ResAsyncParkinglot? {
        try {
            val parkSite = parkSiteInfoService.parkSite

            when (parkSite?.externalSvr) {
                ExternalSvrType.GLNT -> {
                    val response: HttpResponse<JsonNode?>? = restAPIManager.sendPostRequest(
                        "$glntUrl/async/parkinglot",
                        ReqParkinglot(
                            parkinglot = AsyncParkinglot(ip = parkSite.ip!!, name = parkSite.siteName!!, city = parkSite.city!!, address = parkSite.address!! ),
                            facilities = facilityService.allFacilities()!!)
                    )
                    response?.let { it ->
                        if (it.status == 200 || it.status == 201) {
                            val obj = it.body!!.`object`
                            return jacksonObjectMapper().readValue(
                                obj.toString(),
                                ResAsyncParkinglot::class.java
                            ) as ResAsyncParkinglot
                        }
                    }
                }
                else -> {
                    logger.error { "RCS 연계 코드 오류" }
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "RCS Async Parkinglot failed $e" }
        }
        return null
    }

//    fun asyncFacilities(): CommonResult {
//        return CommonResult.data(facilityService.allFacilities())
//    }

//    fun asyncFailureAlarm(request: Failure) {
//        try {
//            logger.info { "Async Failure Alarm $request"  }
//            when (parkinglotService.parkSite!!.externalSvr) {
//                ExternalSvrType.GLNT -> {
//                    restAPIManager.sendPostRequest(
//                        glntUrl+"/parkinglots/facilities/errors",
//                        ReqFailureAlarm(parkinglotId = parkinglotService.parkSite!!.rcsParkId!!, facilityId = request.facilitiesId!!, createDate = request.issueDateTime.toString(), contents = request.failureCode!!)
//                    )
//                }
//            }
//
//        }catch (e: RuntimeException) {
//            logger.error { "RCS Async Failure Alarm $request $e" }
//        }
//    }

//    fun asyncRestoreAlarm(request: Failure) {
//        try {
//            logger.info { "Async Restore Alarm $request"  }
//            when (parkinglotService.parkSite!!.externalSvr) {
//                ExternalSvrType.GLNT -> {
//                    restAPIManager.sendPatchRequest(
//                        glntUrl+"/parkinglots/facilities/errors",
//                        ReqFailureAlarm(parkinglotId = parkinglotService.parkSite!!.rcsParkId!!, facilityId = request.facilitiesId!!, createDate = request.expireDateTime.toString(), contents = request.failureCode!!, resolvedYn = checkUseStatus.Y)
//                    )
//                }
//            }
//        }catch (e: RuntimeException) {
//            logger.error { "RCS Async Restore Alarm $request $e" }
//        }
//
//    }

    fun asyncFacilitiesHealth(request: List<FacilityDTO>) {
        try {
            logger.debug { "Async facilities health $request"  }

            val parkSite = parkSiteInfoService.parkSite

            val result = ArrayList<ReqHealthCheck>()
            request.forEach { it ->
                result.add(ReqHealthCheck(
                    dtFacilitiesId = it.dtFacilitiesId!!,
                    health = it.health,
                    healthDateTime = it.healthDate?.let { DateUtil.formatDateTime(it) }
                ))
            }
            rcsClient.asyncFacilitiesHealth(result, parkSite?.externalSvr?: ExternalSvrType.NONE, parkSite?.rcsParkId?: -1)
        }catch (e: RuntimeException) {
            logger.error { "RCS Async facilities health $request $e" }
        }
    }

//    fun asyncFacilitiesStatus(request: List<ResAsyncFacility>) {
//        try {
//            logger.info { "Async facilities status $request"  }
//            var result = ArrayList<ReqFacilityStatus>()
//            request.forEach { it ->
//                if (it.category == "BREAKER")
//                    result.add(ReqFacilityStatus(
//                        dtFacilitiesId = it.dtFacilitiesId,
//                        status = it.status,
//                        statusDateTime = it.statusDate?.let { DateUtil.formatDateTime(it) }
//                    ))
//            }
//
//            when (parkinglotService.parkSite!!.externalSvr) {
//                ExternalSvrType.GLNT -> {
//                    restAPIManager.sendPatchRequest(
//                        glntUrl+"/parkinglots/"+parkinglotService.parkSite!!.rcsParkId!!+"/facilities",
//                        result
//                    )
//                }
//            }
//        }catch (e: RuntimeException) {
//            logger.error { "RCS Async facilities status $request $e" }
//        }
//    }

    fun facilityAction(facilityId: String, status: String): CommonResult {
        try {
            logger.warn { "RCS 설비 동작 요청 $facilityId status $status" }
            val action = when(status) {
                "UP" -> "open"
                "DOWN" -> "close"
                "UPLOCK" -> "uplock"
                "UNLOCK" -> "unlock"
                else -> status
            }
            when(status) {
                "UP", "DOWN", "UPLOCK", "UNLOCK" -> relayService.actionGate(facilityId, "FACILITI", action)
                "RESET" -> {
                    parkinglotService.getFacilityByDtFacilityId(facilityId)?.let { facility ->
                        facility.resetPort?.let { it ->
                            val port = it.toInt()-1
                            if (port < 0) return CommonResult.error("Reset Action failed")
                            parkinglotService.getGate(facility.gateId)?.let { gate ->
                                val url = gate.resetSvr+port
                                resetClient.sendReset(url).let { response ->
                                    singleTimer()
                                    logger.warn { "RESET $facilityId response ${response!!.status} ${response.body.toString()}" }
                                    if (response!!.status == HttpStatus.SC_OK) {
                                        Observable.timer(2, TimeUnit.SECONDS).subscribe {
                                            logger.info { "reset one more $url" }
                                            resetClient.sendReset(url).let { reResponse ->
                                                logger.warn { "RESET $facilityId response ${reResponse!!.status} ${reResponse.body.toString()}" }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
            return CommonResult.data()
        }catch (e: RuntimeException) {
            logger.error { "RCS Breaker $facilityId status $status $e" }
            return CommonResult.error("RCS Breaker $facilityId status $status failed")
        }
    }

    @Throws(CustomException::class)
    fun getInouts(request: reqSearchParkin): CommonResult {
        try {
            return CommonResult.data(inoutService.getAllParkLists(request))
        }catch (e: CustomException){
            logger.error { "rcs getInouts failed $e" }
            return CommonResult.error("Admin getInouts failed")
        }
    }

    fun getInout(sn: Long) : CommonResult {
        try {
            return CommonResult.data(inoutService.getInout(sn))
        }catch (e: CustomException){
            logger.error { "rcs getInout failed $e" }
            return CommonResult.error("Admin getInout failed")
        }
    }

    @Throws(CustomException::class)
    fun createInout(request: resParkInList) : CommonResult {
        try {
            return CommonResult.data(inoutService.createInout(request).data)
        }catch (e: CustomException){
            logger.error { "rcs createInout failed $e" }
            return CommonResult.error("rcs createInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun calcInout(request: resParkInList) : CommonResult {
        try {
            return CommonResult.data(inoutService.calcInout(request))
        }catch (e: CustomException){
            logger.error { "rcs calcInout failed $e" }
            return CommonResult.error("rcs calcInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun updateInout(request: resParkInList) : CommonResult {
        try {
            return CommonResult.data(inoutService.updateInout(request))
        }catch (e: CustomException){
            logger.error { "rcs updateInout failed $e" }
            return CommonResult.error("rcs updateInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun forcedExit(sn: Long) : CommonResult {
        try {
            return inoutService.deleteInout(sn)
        } catch(e: CustomException){
            logger.error { "rcs forcedExit failed $e" }
            return CommonResult.error("rcs updateInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun getTickets(request: reqSearchProductTicket): CommonResult {
        try {
            return CommonResult.data(productService.getProducts(request))
        }catch (e: CustomException) {
            logger.error { "rcs getTickets failed $e" }
            return CommonResult.error("rcs getTickets failed")
        }
    }


    @Throws(CustomException::class)
    fun createTicket(request: reqCreateProductTicket) : CommonResult {
        try {
            val data = productService.createProduct(request)
            when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(data.data)
                }
                else -> {
                    logger.error { "rcs createTicket failed ${data.msg}" }
                    return CommonResult.error("rcs createTicket failed")
                }
            }
        }catch (e: CustomException) {
            logger.error { "rcs createTicket failed $e" }
            return CommonResult.error("rcs createTicket failed")
        }
    }

    @Throws(CustomException::class)
    fun getDiscountClasses(): CommonResult {
        try {
            return CommonResult.data(discountService.getDiscountClass()!!.filter { it.delYn == DelYn.N && it.rcsUse == true })
        }catch (e: CustomException) {
            logger.error { "rcs getTickets failed $e" }
            return CommonResult.error("rcs getTickets failed")
        }
    }

    @Throws(CustomException::class)
    fun getCorpInfo(corpId: String) : CommonResult {
        try {
            return CommonResult.data(corpService.getStoreById(corpId))
        }catch (e: CustomException) {
            logger.error { "rcs getCorpInfo failed $e" }
            return CommonResult.error("rcs getCorpInfo failed")
        }
    }

    @Throws(CustomException::class)
    fun asyncCallVoip(voipId: String): CommonResult {
        try {
            val parkSite = parkSiteInfoService.parkSite
            val url = glntUrl + "/parkinglots/" + parkSite?.rcsParkId!! + "/call/" + voipId
            logger.warn { "callVoip $url" }
            restAPIManager.sendGetRequest(url)
            return CommonResult.data()
        }catch (e: CustomException) {
            logger.error { "rcs asyncCallVoip failed $voipId $e" }
            return CommonResult.error("rcs asyncCallVoip failed")
        }
    }

//    private fun responseToMap(response: HttpResponse<JsonNode?>): MutableMap<String, Any>
//        = response.body!!.`object`.toMap()

}