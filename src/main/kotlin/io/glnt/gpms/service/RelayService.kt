package io.glnt.gpms.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JSONUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.model.*
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.rcs.model.ReqFacilityStatus
import io.glnt.gpms.handler.relay.model.FacilitiesFailureAlarm
import io.glnt.gpms.handler.relay.model.FacilitiesStatusNoti
import io.glnt.gpms.handler.relay.model.paystationvehicleListSearch
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.common.api.RcsClient
import io.glnt.gpms.common.api.RelayClient
import io.glnt.gpms.model.dto.entity.BarcodeTicketsDTO
import io.glnt.gpms.model.dto.entity.ParkOutDTO
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.*
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
    private val rcsClient: RcsClient,
    private val parkSiteInfoService: ParkSiteInfoService,
    private val relayClient: RelayClient,
    private val parkOutService: ParkOutService,
    private val inoutPaymentService: InoutPaymentService
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
            if (parkSiteInfoService.isTmapSend())
                tmapSendService.sendHealthCheckRequest(request, "")

            request.facilitiesList.forEach { facility ->
                facilityService.updateHealthCheck(facility.dtFacilitiesId, facility.status!!)
//                //2021-10-01 정산 실패 메세지 변경 삭제 처리
//                if (facility.failureAlarm == "icCardReaderFailure" && !facility.responseId.isNullOrEmpty()) {
//                    logger.warn { "정산기 결제 오류 $request" }
//                    // 정산기 요청 한 후 정산 실패 시 강제 오픈 처리 ->
//                    facilityService.getGateByFacilityId(facility.dtFacilitiesId)?.let { it ->
//                        actionGate(it.gateId, "GATE", "open")
//                    }
//                }
            }

            if (parkAlarmSetting.payAlarm == YN.Y && parkAlarmSetting.payLimitTime!! > 0) {
                paymentHealthCheck()
            }

        } catch (e: CustomException){
            logger.error { "facilitiesHealthCheck failed ${e.message}" }
        }

    }

    fun paystationCheck(request: reqRelayHealthCheck) {
        try {
            request.facilitiesList.forEach { list ->
                //2021-10-01 정산 실패 메세지 변경 삭제 처리
                logger.warn { "정산기 ${list.dtFacilitiesId} ${list.failureAlarm} ${list.status}" }
                parkinglotService.getFacilityByDtFacilityId(list.dtFacilitiesId)?.let { facility ->
                    if (list.status?.isNotEmpty() == true) {
                        saveFailure(
                            Failure(
                                sn = null,
                                issueDateTime = LocalDateTime.now(),
                                facilitiesId = list.dtFacilitiesId,
                                fName = facility.fName,
                                failureCode = list.failureAlarm,
                                failureType = list.status,
                                category = facility.category,
                                gateId = facility.gateId
                            )
                        )
                    }
                }
            }
        } catch (e: CustomException){
            logger.error { "paystationCheck failed $e" }
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
                                fName = data.fName,
                                failureCode = "crossingGateLongTimeOpen",
                                failureType = "NORMAL",
                                category = data.category,
                                gateId = data.gateId
                            )
                        )
                    } else {
                        saveFailure(
                            Failure(
                                sn = null,
                                issueDateTime = LocalDateTime.now(),
                                facilitiesId = facility.dtFacilitiesId,
                                fName = data.fName,
                                failureCode = "crossingGateBarDamageDoubt",
                                failureType = "NORMAL",
                                category = data.category,
                                gateId = data.gateId
                            )
                        )
                    }
                }
            }

            if (result.isNotEmpty()) {
                if (parkSiteInfoService.isExternalSend()){
                    if (parkSiteInfoService.isTmapSend()) {
                        tmapSendService.sendFacilitiesStatusNoti(reqTmapFacilitiesStatusNoti(facilitiesList = result), null)
                    } else {
                        facilityService.activeGateFacilities()?.let { list ->
//                        rcsService.asyncFacilitiesStatus(list)
                            val data = ArrayList<ReqFacilityStatus>()
                            list.forEach { it ->
                                if (it.category == FacilityCategoryType.BREAKER)
                                    data.add(ReqFacilityStatus(
                                        dtFacilitiesId = it.dtFacilitiesId!!,
                                        status = it.status,
                                        statusDateTime = it.statusDate?.let { DateUtil.formatDateTime(it) }
                                    ))
                            }
                            rcsClient.asyncFacilitiesStatus(
                                data,
                                parkSiteInfoService.parkSite!!.externalSvr!!,
                                parkSiteInfoService.parkSite!!.rcsParkId!!)
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
                            fName = facility.fName,
                            failureCode = failure.failureAlarm,
                            failureType = failure.status,
                            category = facility.category,
                            gateId = facility.gateId)
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
                    if (parkSiteInfoService.isTmapSend() && failure.status != "normal")
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
            if (parkSiteInfoService.parkSite!!.saleType == SaleType.FREE) return
            val result = ArrayList<FacilitiesFailureAlarm>()
            parkinglotService.getFacilityByCategory(FacilityCategoryType.PAYSTATION)?.let { facilities ->
                facilities.forEach { facility ->
                    inoutService.lastSettleData(facility.facilitiesId!!)?.let { out ->
                        //정산기 마지막 페이 시간 체크
                        if (DateUtil.diffHours(
                                DateUtil.stringToLocalDateTime(out.approveDatetime!!, "yyyy-MM-dd HH:mm:ss"),
                                LocalDateTime.now()) > parkAlarmSetting.payLimitTime!!) {
                            if (parkSiteInfoService.isTmapSend() && result.isNotEmpty())
                                tmapSendService.sendFacilitiesFailureAlarm(FacilitiesFailureAlarm(facilitiesId = facility.facilitiesId!!, failureAlarm = "dailyUnAdjustment"), null)
                            saveFailure(
                                Failure(sn = null,
                                    issueDateTime = LocalDateTime.now(),
                                    facilitiesId = facility.facilitiesId,
                                    fName = facility.fName,
                                    failureCode = "dailyUnAdjustment",
                                    failureType = "dailyUnAdjustment",
                                    category = facility.category,
                                    gateId = facility.gateId)
                            )
                        } else {
                            saveFailure(
                                Failure(sn = null,
                                    issueDateTime = LocalDateTime.now(),
                                    facilitiesId = facility.facilitiesId,
                                    fName = facility.fName,
                                    failureCode = "dailyUnAdjustment",
                                    failureType = "NORMAL",
                                    category = facility.category,
                                    gateId = facility.gateId
                                )
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
                    parkSiteInfoService.parkSite!!.externalSvr?.let { externalSvrType ->
                        if (externalSvrType != ExternalSvrType.NONE)
                            rcsClient.asyncRestoreAlarm(it, externalSvrType, parkSiteInfoService.parkSite!!.rcsParkId!!)
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
                    parkSiteInfoService.parkSite!!.externalSvr?.let { externalSvrType ->
                        if (externalSvrType != ExternalSvrType.NONE)
                            rcsClient.asyncFailureAlarm(request, externalSvrType, parkSiteInfoService.parkSite!!.rcsParkId?: 0)
                    }
                }
            }
        }catch (e: CustomException){
            logger.error { "saveFailure failed ${e.message}" }
        }
    }

//    @Throws(CustomException::class)
//    fun resultPayment(sn: String, request: reqPaymentResult, dtFacilityId: String){
//        logger.info { "resultPayment request $request facilityId $dtFacilityId" }
//
////        val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqPaymentResult::class.java)
//
//        facilityService.getGateByFacilityId(dtFacilityId)?.let { facility ->
//            inoutService.paymentResult(request, sn, facility.gateId)
////            relayClient.sendPayStation(
////                gateId = facility.gateId,
////                type = "paymentResponse",
////                requestId = request.requestId!!,
////                data = reqPaymentResponse(
////                    chargingId = request.transactionId,
////                    vehicleNumber = request.vehicleNumber
////                ))
//        }
////        val gateId = parkinglotService.getFacilityByDtFacilityId(dtFacilityId)!!.gateId
////        inoutService.paymentResult(contents, request.requestId!!, gateId)
////
////        relayClient.sendPayStation(
////            gateId = gateId,
////            type = "paymentResponse",
////            requestId = request.requestId!!,
////            data = reqPaymentResponse(
////                chargingId = contents.transactionId,
////                vehicleNumber = contents.vehicleNumber
////        ))
////        facilityService.sendPaystation(
////            reqPaymentResponse(
////                chargingId = contents.transactionId,
////                vehicleNumber = contents.vehicleNumber
////            ),
////            gate = gateId,
////            requestId = request.requestId!!,
////            type = "paymentResponse"
////        )
//    }

    @Throws(CustomException::class)
    fun searchCarNumber(request: reqApiTmapCommon, dtFacilityId: String){
        logger.info { "searchCarNumber request $request facilityId $dtFacilityId" }
//        request.contents = JSONUtil.getJsObject(request.contents)
        val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqSendVehicleListSearch::class.java)
        request.requestId = parkSiteInfoService.generateRequestId()

        if (parkSiteInfoService.isTmapSend()) {
            // table db insert
            val requestId = parkSiteInfoService.generateRequestId()
            vehicleListSearchRepository.save(VehicleListSearch(requestId = requestId, facilityId = parkinglotService.getFacilityByDtFacilityId(dtFacilityId)!!.facilitiesId))
            tmapSendService.sendTmapInterface(request, requestId, "vehicleListSearch")
        } else {
            val parkIns = inoutService.searchParkInByVehicleNo(contents.vehicleNumber, "").filter { it.outSn == 0L }
            parkinglotService.getGateInfoByDtFacilityId(dtFacilityId)?.let { gate ->
                val data = ArrayList<paystationvehicleListSearch>()
                if (!parkIns.isNullOrEmpty()) {
                    parkIns.forEach {
                        data.add(
                            paystationvehicleListSearch(
                                inSn = it.sn!!.toString(),
                                vehicleNumber = it.vehicleNo!!,
                                imageUrl = it.image?.let { image -> image.substring(image.indexOf("/park")) }?: kotlin.run { "/park/noImage.jpg" },
                                inVehicleDateTime = DateUtil.formatDateTime(it.inDate!!, "yyyy-MM-dd HH:mm:ss")
                            )
                        )
                    }
                } else {
                    // todo 사전 정산기 요청 시 게이트 오픈 skip
                    // 번호 검색 없을 시 게이트 오픈
                    if (gate.gateType == GateTypeStatus.OUT)
                        inoutService.outFacilityIF("UNRECOGNIZED", "", gate, null, 0)
                }

                relayClient.sendPayStation(
                    gate.gateId,
                    "vehicleListSearchResponse",
                    request.requestId!!,
                    reqVehicleSearchList(
                        vehicleList = data,
                        result = "SUCCESS"
                    ),
                    dtFacilityId
                )
//                facilityService.sendPaystation(
//                    reqVehicleSearchList(
//                        vehicleList = data,
//                        result = "SUCCESS"
//                    ),
//                    gate = gate.gateId,
//                    requestId = request.requestId!!,
//                    type = "vehicleListSearchResponse"
//                )
            }
        }
    }

//    @Throws(CustomException::class)
//    fun requestAdjustment(request: reqApiTmapCommon, dtFacilityId: String){
//        try {
//            val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqAdjustmentRequest::class.java)
//            request.requestId = parkSiteInfoService.generateRequestId()
//
//            if (parkSiteInfoService.isTmapSend()) {
//                // table db insert
//            } else {
//                val gateId = parkinglotService.getGateInfoByDtFacilityId(dtFacilityId)!!.gateId
//                // 사전 정산 시 현재 기준으로 금액 계산만 처리 -> 정산쪽만 연계
//
//                // 출차 정산 시 출차 형식 따름
//                inoutService.parkOut(reqAddParkOut(vehicleNo = contents.vehicleNumber,
//                                                   dtFacilitiesId = parkingFacilityRepository.findByGateIdAndCategory(gateId, FacilityCategoryType.LPR)!![0].dtFacilitiesId,
//                                                   date = LocalDateTime.now(),
//                                                   resultcode = "0",
//                                                   uuid = JSONUtil.generateRandomBasedUUID()))
//            }
//
//        }catch (e: CustomException){
//            logger.error { "saveFailure failed ${e.message}" }
//        }
//    }

    fun aplyDiscountTicket(request: reqApiTmapCommon, dtFacilityId: String){
        logger.warn { "정산기 $dtFacilityId 할인 티켓 입력 ${request.contents}" }

        try {
            val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqDiscountTicket::class.java)

            val info = barcodeService.findAll().filter {
                it.effectDate!! <= LocalDateTime.now() && it.expireDate!! >= LocalDateTime.now() && it.delYn == YN.N}.get(0)

            // inout_payment sn
            val sn = request.requestId!!.toLong()
            inoutPaymentService.findOne(sn).ifPresent { inoutPayment ->
                parkinglotService.getGateInfoByDtFacilityId(dtFacilityId )?.let { gate ->
                    // 사전 정산기
                    if (gate.gateType == GateTypeStatus.ETC) {
                        parkInRepository.findBySn(inoutPayment.inSn?: -1)?.let { parkIn ->
                            val barcodeTicketDTO =  BarcodeTicketsDTO(
                                barcode = contents.barcode,
                                inSn = parkIn.sn,
                                applyDate = DateUtil.stringToLocalDateTime(request.eventDateTime!!),
                                price = contents.barcode?.substring(info.startIndex!!, info.endIndex!!)?.toInt(),
                                vehicleNo = contents.vehicleNumber, delYn = YN.N
                            )

                            val barcodeClass = barcodeClassService.findByStartLessThanEqualAndEndGreaterThanAndDelYn(barcodeTicketDTO.price!!)

                            if (barcodeClass != null) {
                                // 입차할인권 save
                                barcodeTicketDTO.inSn?.let {
                                    discountService.saveInoutDiscount(
                                        InoutDiscount(
                                            sn = null, discontType = TicketType.BARCODE, discountClassSn = barcodeClass.discountClassSn, inSn = it,
                                            quantity = 1, useQuantity = 1, delYn = YN.N, applyDate = barcodeTicketDTO.applyDate))
                                }
                            }
                            barcodeTicketService.save(barcodeTicketDTO)

                            val price = if ( parkinglotService.isPaid()) {
                                inoutService.calcParkFee("OUT", parkIn.inDate!!, LocalDateTime.now(), VehicleType.SMALL, parkIn.vehicleNo ?: "", parkIn.sn ?: -1)
                            } else null

                            // 정산 처리
                            val parkOutDTO = ParkOutDTO(
                                inSn = parkIn.sn,
                                vehicleNo = parkIn.vehicleNo,
                                payfee = price?.totalPrice ?: 0,
                                parktime = price?.parkTime ?: DateUtil.diffMins(parkIn.inDate!!, LocalDateTime.now()),
                                discountfee = price?.discountPrice ?: 0,
                                dayDiscountfee = price?.dayilyMaxDiscount ?: 0,
                                parkfee = price?.orgTotalPrice ?: 0
                            )
                            inoutService.waitFacilityIF("PREPAYMENT",parkIn.parkcartype ?: "", parkIn.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!, dtFacilityId)
                        }
                    } else {
                        // 일반 출차
                        // parkOut 확인
                        parkOutRepository.findBySn(inoutPayment.outSn?: -1).ifPresent {
                            it.inSn
                            val barcodeTicketDTO =  BarcodeTicketsDTO(
                                barcode = contents.barcode,
                                inSn = it.inSn,
                                applyDate = DateUtil.stringToLocalDateTime(request.eventDateTime!!),
                                price = contents.barcode?.substring(info.startIndex!!, info.endIndex!!)?.toInt(),
                                vehicleNo = contents.vehicleNumber, delYn = YN.N
                            )

                            val barcodeClass = barcodeClassService.findByStartLessThanEqualAndEndGreaterThanAndDelYn(barcodeTicketDTO.price!!)

                            if (barcodeClass != null) {
                                // 입차할인권 save
                                barcodeTicketDTO.inSn?.let {
                                    discountService.saveInoutDiscount(
                                        InoutDiscount(
                                            sn = null, discontType = TicketType.BARCODE, discountClassSn = barcodeClass.discountClassSn, inSn = it,
                                            quantity = 1, useQuantity = 1, delYn = YN.N, applyDate = barcodeTicketDTO.applyDate))
                                }
                            }
                            barcodeTicketService.save(barcodeTicketDTO)
                            // 금액 계산
                            parkInRepository.findBySn(it.inSn ?: -1)?.let { parkIn ->
                                val price = inoutService.calcParkFee("OUT", parkIn.inDate!!, it.outDate!!, VehicleType.SMALL, parkIn.vehicleNo ?: "", parkIn.sn ?: -1)
                                val parkOutDTO = parkOutService.save(
                                    ParkOutDTO(
                                        sn = it.sn,
                                        gateId = it.gateId,
                                        parkcartype = it.parkcartype,
                                        vehicleNo = it.vehicleNo,
                                        image = it.image,
                                        resultcode = it.resultcode,
                                        requestid = it.requestid,
                                        fileuploadid = it.fileuploadid,
                                        outDate = it.outDate,
                                        uuid = it.uuid,
                                        parktime = it.parktime,
                                        parkfee = price?.orgTotalPrice ?: 0,
                                        payfee = price?.totalPrice ?: 0,
                                        discountfee = price?.discountPrice ?: 0,
                                        dayDiscountfee = price?.dayilyMaxDiscount ?: 0,
                                        delYn = YN.N,
                                        inSn = it.inSn,
                                        originDayDiscountFee = price?.dayilyMaxDiscount ?: 0,
                                        originParkFee = price?.orgTotalPrice ?: 0,
                                        originDiscountFee = price?.discountPrice ?: 0,
                                        originParkTime = it.parktime,
                                        originPayFee = price?.totalPrice ?: 0
                                    )
                                )
                                // 정산 대기 처리
                                inoutService.waitFacilityIF("PAYMENT", it.parkcartype!!, it.vehicleNo!!,
                                    parkinglotService.getGate(it.gateId ?: "")!!,
                                    parkOutDTO,
                                    parkIn.inDate!!)

                                if ((price?.totalPrice?: 0) <= 0) {
                                    inoutService.outFacilityIF(it.parkcartype!!, it.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
                                }
                            }
                        }
                    }
                }
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

