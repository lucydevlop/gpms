package io.glnt.gpms.handler.relay.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JSONUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.handler.facility.model.*
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqAddParkOut
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.inout.service.checkItemsAre
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.rcs.model.ReqFacilityStatus
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.handler.relay.model.FacilitiesFailureAlarm
import io.glnt.gpms.handler.relay.model.FacilitiesStatusNoti
import io.glnt.gpms.handler.relay.model.paystationvehicleListSearch
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.io.glnt.gpms.common.api.RcsClient
import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.*
import io.glnt.gpms.service.BarcodeClassService
import io.glnt.gpms.service.BarcodeService
import io.glnt.gpms.service.BarcodeTicketService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
class RelayService(
    private val barcodeService: BarcodeService,
    private val parkOutRepository: ParkOutRepository,
    private val barcodeClassService: BarcodeClassService,
    private val discountService: DiscountService,
    private val barcodeTicketService: BarcodeTicketService,
    private val parkInRepository: ParkInRepository,
    private val rcsClient: RcsClient
) {
    companion object : KLogging()

    lateinit var parkAlarmSetting: ParkAlarmSetting

    @Autowired
    private lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var facilityService: FacilityService

    @Autowired
    private lateinit var inoutService: InoutService

//    @Autowired
//    private lateinit var rcsService: RcsService

    @Autowired
    private lateinit var restAPIManager: RestAPIManagerUtil

    @Autowired
    private lateinit var parkAlarmSettingRepository: ParkAlarmSetttingRepository

    @Autowired
    private lateinit var failureRepository: FailureRepository

    @Autowired
    private lateinit var parkingFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var vehicleListSearchRepository: VehicleListSearchRepository

    @PostConstruct
    fun fetchParkAlarmSetting() {
        parkAlarmSettingRepository.findTopByOrderBySiteid()?.let { it ->
            parkAlarmSetting = it
        }
    }

    fun facilitiesHealthCheck(request: reqRelayHealthCheck) {
        logger.trace { "facilitiesHealthCheck $request" }
        try {
            if (parkinglotService.isTmapSend())
                tmapSendService.sendHealthCheckRequest(request, "")

            request.facilitiesList.forEach { facility ->
                facilityService.updateHealthCheck(facility.dtFacilitiesId, facility.status!!)
                if (facility.failureAlarm == "icCardReaderFailure" && !facility.responseId.isNullOrEmpty()) {
                    logger.warn { "정산기 결제 오류 $request" }
                    // 정산기 요청 한 후 정산 실패 시 강제 오픈 처리
                    facilityService.getGateByFacilityId(facility.dtFacilitiesId)?.let { it ->
                        actionGate(it.gateId, "GATE", "open")
                    }
                }
            }

            if (parkAlarmSetting.payAlarm == checkUseStatus.Y && parkAlarmSetting.payLimitTime!! > 0) {
                paymentHealthCheck()
            }

        } catch (e: CustomException){
            logger.error { "facilitiesHealthCheck failed ${e.message}" }
        }

    }

    fun statusNoti(request: reqRelayHealthCheck) {
        logger.trace { "statusNoti $request" }
        try {
            val result = ArrayList<FacilitiesStatusNoti>()

            request.facilitiesList.forEach { facility ->
                val data = facilityService.updateStatusCheck(facility.dtFacilitiesId, facility.status!!)
                if (data != null) {
                    result.add(FacilitiesStatusNoti(facilitiesId = data.facilitiesId, STATUS = facility.status!!))
                    // close 상태 수신 시 error 상태 check
                    if (facility.status == "DOWN") {
                        saveFailure(
                            Failure(
                                sn = null,
                                issueDateTime = LocalDateTime.now(),
                                facilitiesId = facility.dtFacilitiesId,
                                fName = data.fname,
                                failureCode = "crossingGateLongTimeOpen",
                                failureType = "NORMAL"
                            )
                        )
                    } else {
                        saveFailure(
                            Failure(
                                sn = null,
                                issueDateTime = LocalDateTime.now(),
                                facilitiesId = facility.dtFacilitiesId,
                                fName = data.fname,
                                failureCode = "crossingGateBarDamageDoubt",
                                failureType = "NORMAL"
                            )
                        )
                    }
                }
            }

            if (result.isNotEmpty()) {
                if (parkinglotService.isTmapSend()) {
                    tmapSendService.sendFacilitiesStatusNoti(reqTmapFacilitiesStatusNoti(facilitiesList = result), null)
                }

                if (parkinglotService.isExternalSend()){
                    facilityService.activeGateFacilities()?.let { list ->
//                        rcsService.asyncFacilitiesStatus(list)
                        var result = ArrayList<ReqFacilityStatus>()
                        list.forEach { it ->
                            if (it.category == FacilityCategoryType.BREAKER)
                                result.add(ReqFacilityStatus(
                                    dtFacilitiesId = it.dtFacilitiesId!!,
                                    status = it.status,
                                    statusDateTime = it.statusDate?.let { DateUtil.formatDateTime(it) }
                                ))
                        }
                        parkinglotService.parkSite!!.externalSvr?.let {
                            rcsClient.asyncFacilitiesStatus(result, it, parkinglotService.parkSite!!.rcsParkId!!)
                        }
                    }
                }
            }

        } catch (e: CustomException){
            logger.error { "statusNoti failed ${e.message}" }
        }
    }

    fun failureAlarm(request: reqRelayHealthCheck) {
        logger.info { "failureAlarm $request" }
        try {
            request.facilitiesList.forEach { failure ->
                parkinglotService.getFacilityByDtFacilityId(failure.dtFacilitiesId)?.let { facility ->
                    // 정산기 정상, 비정상
                    saveFailure(
                        Failure(sn = null,
                            issueDateTime = LocalDateTime.now(),
                            facilitiesId = failure.dtFacilitiesId,
                            fName = facility.fname,
                            failureCode = failure.failureAlarm,
                            failureType = failure.status)
                    )
                    if (facility.category == FacilityCategoryType.PAYSTATION)
                        facilityService.updateStatusCheck(facility.facilitiesId!!, facility.status!!)
//                    }
//                    if (failure.failureAlarm == "crossingGateBarDamageDoubt") {
//                            // 차단기
//                            saveFailure(
//                                Failure(sn = null,
//                                    issueDateTime = LocalDateTime.now(),
////                                        expireDateTime = LocalDateTime.now(),
//                                    facilitiesId = failure.facilitiesId,
//                                    fName = facility.fname,
//                                    failureCode = failure.failureAlarm,
//                                    failureType = failure.failureAlarm)
//                            )
//                        } else {
//                            // 정산기
//                            if (facility.category == "PAYSTATION") {
//                                if (failure.healthStatus == "Normal") {
//                                    facilityService.updateHealthCheck(failure.facilitiesId, failure.healthStatus!!)
//                                } else {
//                                    facilityService.updateHealthCheck(failure.facilitiesId, failure.failureAlarm!!)
//                                }
//                                saveFailure(
//                                    Failure(sn = null,
//                                        issueDateTime = LocalDateTime.now(),
////                                        expireDateTime = LocalDateTime.now(),
//                                        facilitiesId = facility.facilitiesId,
//                                        fName = facility.fname,
//                                        failureCode = failure.failureAlarm!!,
//                                        failureType = failure.healthStatus)
//                                )
//                            }
//                        }
//                    }
                    if (parkinglotService.isTmapSend() && failure.status != "normal")
                        tmapSendService.sendFacilitiesFailureAlarm(FacilitiesFailureAlarm(facilitiesId = facility.facilitiesId, failureAlarm = failure.failureAlarm!!), null)
                }
            }

        } catch (e: CustomException){
            logger.error { "failureAlarm failed ${e.message}" }
        }
    }

    fun paymentHealthCheck() {
        logger.trace { "paymentHealthCheck" }
        try {
            // 무료 주차장은 정산여부 CHECK skip
            if (parkinglotService.parkSite!!.saleType == SaleType.FREE) return
            val result = ArrayList<FacilitiesFailureAlarm>()
            parkinglotService.getFacilityByCategory(FacilityCategoryType.PAYSTATION)?.let { facilities ->
                facilities.forEach { facility ->
                    inoutService.lastSettleData(facility.facilitiesId!!)?.let { out ->
                        //정산기 마지막 페이 시간 체크
                        if (DateUtil.diffHours(
                                DateUtil.stringToLocalDateTime(out.approveDatetime!!, "yyyy-MM-dd HH:mm:ss"),
                                LocalDateTime.now()) > parkAlarmSetting.payLimitTime!!) {
                            if (parkinglotService.isTmapSend() && result.isNotEmpty())
                                tmapSendService.sendFacilitiesFailureAlarm(FacilitiesFailureAlarm(facilitiesId = facility.facilitiesId!!, failureAlarm = "dailyUnAdjustment"), null)
                            saveFailure(
                                Failure(sn = null,
                                    issueDateTime = LocalDateTime.now(),
                                    facilitiesId = facility.facilitiesId,
                                    fName = facility.fname,
                                    failureCode = "dailyUnAdjustment",
                                    failureType = "dailyUnAdjustment")
                            )
                        } else {
                            saveFailure(
                                Failure(sn = null,
                                        issueDateTime = LocalDateTime.now(),
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
        try {
            if (request.failureType!!.toUpperCase() == "NORMAL") {
                failureRepository.findTopByFacilitiesIdAndFailureCodeAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(
                    request.facilitiesId!!,
                    request.failureCode!!
                )?.let {
                    it.expireDateTime = LocalDateTime.now()
                    failureRepository.save(it)
//                    rcsService.asyncRestoreAlarm(it)
                    parkinglotService.parkSite!!.externalSvr?.let { externalSvrType ->
                        rcsClient.asyncRestoreAlarm(it, externalSvrType, parkinglotService.parkSite!!.rcsParkId!!)
                    }

                }
            } else {
                logger.info { "saveFailure $request" }
                failureRepository.findTopByFacilitiesIdAndFailureCodeAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(
                    request.facilitiesId!!,
                    request.failureCode!!
                )?.let { it ->
                    it.failureFlag = it.failureFlag!! + 1
                    it.expireDateTime = null
                    failureRepository.save(it)
                    //rcsService.asyncFailureAlram(it)
                }?: run {
                    failureRepository.save(request)
//                    rcsService.asyncFailureAlarm(request)
                    parkinglotService.parkSite!!.externalSvr?.let { externalSvrType ->
                        rcsClient.asyncFailureAlarm(request, externalSvrType, parkinglotService.parkSite!!.rcsParkId!!)
                    }
                }
            }
        }catch (e: CustomException){
            logger.error { "saveFailure failed ${e.message}" }
        }
    }

    @Throws(CustomException::class)
    fun resultPayment(request: reqApiTmapCommon, dtFacilityId: String){
        logger.info { "resultPayment request $request facilityId $dtFacilityId" }
//        request.contents = JSONUtil.getJsObject(request.contents)
        val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqPaymentResult::class.java)
        val gateId = parkinglotService.getFacilityByDtFacilityId(dtFacilityId)!!.gateId
        facilityService.sendPaystation(
            reqPaymentResponse(
                chargingId = contents.transactionId,
                vehicleNumber = contents.vehicleNumber
            ),
            gate = gateId,
            requestId = request.requestId!!,
            type = "paymentResponse"
        )
        inoutService.paymentResult(contents, request.requestId!!, gateId)
    }

    @Throws(CustomException::class)
    fun searchCarNumber(request: reqApiTmapCommon, dtFacilityId: String){
        logger.info { "searchCarNumber request $request facilityId $dtFacilityId" }
//        request.contents = JSONUtil.getJsObject(request.contents)
        val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqSendVehicleListSearch::class.java)
        request.requestId = parkinglotService.generateRequestId()

        if (parkinglotService.isTmapSend()) {
            // table db insert
            val requestId = parkinglotService.generateRequestId()
            vehicleListSearchRepository.save(VehicleListSearch(requestId = requestId, facilityId = parkinglotService.getFacilityByDtFacilityId(dtFacilityId)!!.facilitiesId))
            tmapSendService.sendTmapInterface(request, requestId, "vehicleListSearch")
        } else {
            val parkIns = inoutService.searchParkInByVehicleNo(contents.vehicleNumber, "")
            val data = ArrayList<paystationvehicleListSearch>()
            if (!parkIns.isNullOrEmpty()) {
                parkIns.filter { it.outSn == 0L }.forEach {
                    data.add(
                        paystationvehicleListSearch(
                            vehicleNumber = it.vehicleNo!!,
                            inVehicleDateTime = DateUtil.formatDateTime(it.inDate!!, "yyyy-MM-dd HH:mm:ss")))
                }
            }

//             when(parkins.code) {
//                ResultCode.SUCCESS.getCode() -> {
//                    val lists = parkins.data as? List<*>?
//                    lists!!.checkItemsAre<ParkIn>()?.filter { it.outSn == 0L }?.let { list ->
//                        list.forEach {
//                            data.add(
//                                paystationvehicleListSearch(
//                                    vehicleNumber = it.vehicleNo!!,
//                                    inVehicleDateTime = DateUtil.formatDateTime(it.inDate!!, "yyyy-MM-dd HH:mm:ss")))
//                        }
//
//                    }
//                }
//            }

            facilityService.sendPaystation(
                reqVehicleSearchList(
                    vehicleList = data,
                    result = "SUCCESS"
                ),
                gate = parkinglotService.getFacilityByDtFacilityId(dtFacilityId)!!.gateId,
                requestId = request.requestId!!,
                type = "vehicleListSearchResponse"
            )
        }
    }

    @Throws(CustomException::class)
    fun requestAdjustment(request: reqApiTmapCommon, dtFacilityId: String){
        logger.info { "requestAdjustment request $request facilityId $dtFacilityId" }
        try {
            val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqAdjustmentRequest::class.java)
            request.requestId = parkinglotService.generateRequestId()

            if (parkinglotService.isTmapSend()) {
                // table db insert
//                val requestId = parkinglotService.generateRequestId()
//                vehicleListSearchRepository.save(VehicleListSearch(requestId = requestId, facilityId = facilityId))
//                tmapSendService.sendTmapInterface(request, requestId, "vehicleListSearch")
            } else {
                val gateId = parkinglotService.getGateInfoByDtFacilityId(dtFacilityId)!!.gateId
                inoutService.parkOut(reqAddParkOut(vehicleNo = contents.vehicleNumber,
                                                   dtFacilitiesId = parkingFacilityRepository.findByGateIdAndCategory(gateId, FacilityCategoryType.LPR)!![0].dtFacilitiesId,
                                                   date = LocalDateTime.now(),
                                                   resultcode = "0",
                                                   uuid = JSONUtil.generateRandomBasedUUID()))
            }

        }catch (e: CustomException){
            logger.error { "saveFailure failed ${e.message}" }
        }
    }

    fun aplyDiscountTicket(request: reqApiTmapCommon, dtFacilityId: String){
        logger.info { "정산기 $dtFacilityId 할인 티켓 입력 $request" }

        try {
            val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqDiscountTicket::class.java)

            val info = barcodeService.findAll().filter {
                it.effectDate!! <= LocalDateTime.now() && it.expireDate!! >= LocalDateTime.now() && it.delYn == DelYn.N}.get(0)

            // parkOut 확인
            parkOutRepository.findBySn(request.requestId!!.toLong())?.let {
                it.inSn
                var barcodeTicketDTO: BarcodeTicketsDTO =  BarcodeTicketsDTO(
                                        barcode = contents.parkTicketNumber,
                                        inSn = it.inSn,
                                        applyDate = DateUtil.stringToLocalDateTime(request.eventDateTime!!),
                                        price = contents.parkTicketNumber.substring(info.startIndex!!, info.endIndex!!).toInt(),
                                        vehicleNo = contents.vehicleNumber, delYn = DelYn.N
                                        )

                var barcodeClass = barcodeClassService.findByStartLessThanEqualAndEndGreaterThanAndDelYn(barcodeTicketDTO.price!!)

                if (barcodeClass != null) {
                    // 입차할인권 save
                    barcodeTicketDTO.inSn?.let {
                        discountService.saveInoutDiscount(
                            InoutDiscount(
                                sn = null, discontType = TicketType.BARCODE, discountClassSn = barcodeClass.discountClassSn, inSn = it,
                                quantity = 1, useQuantity = 1, delYn = DelYn.N, applyDate = barcodeTicketDTO.applyDate))
                    }
                }
                barcodeTicketService.save(barcodeTicketDTO)
                val gateId = parkinglotService.getGateInfoByDtFacilityId(dtFacilityId)!!.gateId
                inoutService.parkOut(reqAddParkOut(vehicleNo = contents.vehicleNumber,
                    dtFacilitiesId = parkingFacilityRepository.findByGateIdAndCategory(gateId, FacilityCategoryType.LPR)!![0].dtFacilitiesId,
                    date = it.outDate!!,
                    resultcode = it.resultcode!!.toString(),
                    uuid = JSONUtil.generateRandomBasedUUID(),
                    parkIn = parkInRepository.findBySn(it.inSn!!)
                ))
            }

        }catch (e: CustomException){
            logger.error { "saveFailure failed ${e.message}" }
        }
    }

    fun actionGate(id: String, type: String, action: String, manual: String? = null) {
        logger.warn { "GATE $action $type $id" }
        try {
            when (type) {
                "GATE" -> {
                    parkinglotService.getFacilityByGateAndCategory(id, FacilityCategoryType.BREAKER)?.let { its ->
                        its.forEach {
                            val url = getRelaySvrUrl(id)
                            if (manual.isNullOrEmpty()) {
                                restAPIManager.sendGetRequest(
                                    url+"/breaker/${it.dtFacilitiesId}/$action"
                                )
                            }else {
                                restAPIManager.sendGetRequest(
                                    url+"/breaker/${it.dtFacilitiesId}/$action/manual"
                                )
                            }
                        }
                    }
                }
                else -> {
                    val url = getRelaySvrUrl(parkinglotService.getFacility(id)!!.gateId)
                    restAPIManager.sendGetRequest(
                        url+"/breaker/${id}/$action"
                    )
                }
            }
        } catch (e: RuntimeException) {
            logger.error {  "$action Gate $type $id error ${e.message}"}
        }
    }

    fun sendDisplayInitMessage(): CommonResult {
        try {
            val result = ArrayList<HashMap<String, Any>>()

            result.add(hashMapOf<String, Any>(
                "in" to inoutService.makeParkPhrase("INIT", "-", "-", "IN"),
                "out" to inoutService.makeParkPhrase("INIT", "-", "-", "OUT")
            ))
            return CommonResult.data(result)
        }catch (e: RuntimeException) {
            logger.error { "sendDisplayInitMessage $e"}
            return CommonResult.notfound("init message not found")
        }
    }

    fun sendDisplayMessage(data: Any, gateId: String, reset: String, type: String) {
        logger.warn { "sendDisplayMessage request $data $gateId" }
        //양방향인 경우
        parkinglotService.getGate(gateId)?.let { gate ->
            if (gate.gateType == GateTypeStatus.IN_OUT) {
                val type = if (type == "IN") LprTypeStatus.INFRONT else LprTypeStatus.OUTFRONT
                parkinglotService.getFacilityGateAndCategoryAndLprType(gateId, FacilityCategoryType.DISPLAY, type)?.let { its ->
                    its.forEach {
                        restAPIManager.sendPostRequest(
                            getRelaySvrUrl(gateId)+"/display/show",
                            reqSendDisplay(it.dtFacilitiesId, data as ArrayList<reqDisplayMessage>, reset)
                        )
                    }
                }
            } else {
                parkinglotService.getFacilityByGateAndCategory(gateId, FacilityCategoryType.DISPLAY)?.let { its ->
                    its.forEach {
                        restAPIManager.sendPostRequest(
                            getRelaySvrUrl(gateId)+"/display/show",
                            reqSendDisplay(it.dtFacilitiesId, data as ArrayList<reqDisplayMessage>, reset)
                        )
                    }
                }
            }
        }
    }

    fun sendDisplayInfo() : CommonResult {
        try {
            return facilityService.getDisplayInfo()?.let {
                CommonResult.data(hashMapOf<String, Any?>(
                    "line1" to it.line1Status,
                    "line2" to it.line2Status
                ))
            }?: kotlin.run {
                CommonResult.notfound("sendDisplayStatus not found")
            }
        }catch (e: RuntimeException) {
            logger.error { "sendDisplayStatus $e"}
            return CommonResult.notfound("sendDisplayStatus not found")
        }
    }

    fun sendUpdateDisplayInfo(data: DisplayInfo) {
        logger.warn { "sendUpdateDisplayInfo request $data" }
        getAllRelaySvrUrl().let { its ->
            its.forEach {
                restAPIManager.sendPatchRequest(
                    it+"/display/format",
                    hashMapOf<String, Any?>(
                        "line1" to data.line1Status,
                        "line2" to data.line2Status
                    )
                )
            }
        }
    }

//    fun callVoip(voipId: String) : CommonResult {
//        try {
//            return rcsService.asyncCallVoip(voipId)
//        }catch (e: RuntimeException) {
//            logger.error { "sendDisplayStatus $e"}
//            return CommonResult.error("send call voip $voipId")
//        }
//    }

    private fun getRelaySvrUrl(gateId: String): String {
        return facilityService.gates.filter { it.gateId == gateId }[0].relaySvr!!
//        return "http://192.168.20.30:9999/v1"
    }

    private fun getAllRelaySvrUrl(): ArrayList<String> {
        val svrs = ArrayList<String>()

        facilityService.gates.forEach { svrs.add(it.relaySvr!!) }
        svrs.sort()

        return ArrayList(svrs.distinct())
    }




//    fun searchCarNumber(request: reqSendVehicleListSearch): CommonResult? {
//        logger.info { "searchCarNumber request $request" }
//        if (parkinglotService.parkSite.tmapSend == "ON") {
//            // table db insert
//            val requestId = parkinglotService.generateRequestId()
//            vehicleListSearchRepository.save(VehicleListSearch(requestId = requestId, facilityId = request.facilityId))
//            tmapSendService.sendTmapInterface(request, requestId, "vehicleListSearch")
//        } else {
//            return inoutService.searchParkInByVehicleNo(request.vehicleNumber, "")
//        }
//        return null
//    }

//    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
//        val data = JSONUtil.getJSONObject(any)
//        val factory = JsonFactory()
//        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
//        return jacksonObjectMapper().readValue(data.toString(), valueType)
//    }
}

