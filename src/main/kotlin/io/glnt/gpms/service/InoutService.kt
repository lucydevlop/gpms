package io.glnt.gpms.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.RelayClient
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.model.BasicPrice
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.service.DiscountService
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqPayData
import io.glnt.gpms.handler.facility.model.reqPayStationData
import io.glnt.gpms.handler.facility.model.reqPaymentResult
import io.glnt.gpms.handler.inout.model.*
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.model.criteria.ParkInCriteria
import io.glnt.gpms.model.criteria.ParkOutCriteria
import io.glnt.gpms.model.dto.*
import io.glnt.gpms.model.dto.entity.*
import io.glnt.gpms.model.dto.request.ReqAddParkingDiscount
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
open class InoutService(
    private var inoutPaymentRepository: InoutPaymentRepository,
    private var feeCalculation: FeeCalculation,
    private var relayClient: RelayClient,
    private var parkInQueryService: ParkInQueryService,
    private var parkInService: ParkInService,
    private var parkOutQueryService: ParkOutQueryService,
    private var parkSiteInfoService: ParkSiteInfoService,
    private var gateService: GateService,
    private val inoutPaymentService: InoutPaymentService,
    private val parkInMapper: ParkInMapper,
    private val displayService: DisplayService,
    private val ticketService: TicketService
) {
    companion object : KLogging()

    @Value("\${image.filepath}")
    lateinit var imagePath: String

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var tmapSendService: TmapSendService

    @Autowired
    lateinit var facilityService: FacilityService

    @Autowired
    lateinit var discountService: DiscountService

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    @Autowired
    private lateinit var parkOutRepository: ParkOutRepository

    private fun isOpenGate(gate: GateDTO, inDate: LocalDateTime, parkCarType: String): Boolean {
        when(gate.openAction) {
            OpenActionType.NONE -> return true
            OpenActionType.RECOGNITION ->
                return parkCarType != "UNRECOGNIZED"
            OpenActionType.RESTRICT ->
                return !(parkCarType.contains("RECOGNIZED") || parkCarType.contains("NORMAL"))
            else -> {
                gate.openType?.let { types ->
                    val hhmm = DateUtil.getHourMinuteByLocalDateTime(inDate)
                    types.forEach { type ->
                        if (type["startTime"].toString() >= hhmm && type["endTime"].toString() < hhmm) {
                            if (type["openAction"] ==  OpenActionType.NONE) return true

                            if (type["openAction"] ==  OpenActionType.RECOGNITION && parkCarType != "UNRECOGNIZED") {
                                return true
                            }

                            if (type["openAction"] == OpenActionType.RESTRICT && !(parkCarType.contains("RECOGNIZED") || parkCarType.contains("NORMAL"))) {
                                return true
                            }
                        }
                    }
                }
            }
        }
        return false
    }


    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        try {

            // gate up(option check)
            parkinglotService.getGateInfoByDtFacilityId(dtFacilitiesId) ?.let { gate ->
                requestId = parkSiteInfoService.generateRequestId()

                // 이전 입차 건에 대한 open 여부 확인
                var beforeOpen = false
                beforeParkIn?.let {
                    parkInService.findOneByUuid(it.uuid?: "-")?.let { parkIns ->
                        parkIns.forEach { parkInDTO ->
                            beforeOpen = beforeOpen || isOpenGate(gateService.findOne(parkInDTO.gateId?:"")!!,parkInDTO.inDate!!, parkInDTO.parkcartype?: "NORMAL")
                            if (parkInDTO.parkcartype!!.contains("RECOGNIZED")) request.inSn = parkInDTO.sn
                        }
                    }
                } ?: run {
                    false
                }

                // 방문차량 입차통보 데이터
                var visitorData:reqVisitorExternal? = null

                // 차량번호 정상인식에 한하여 아래의 모듈 처리
                if (request.recognitionResult.equals("RECOGNITION")) {
                    ticketService.getValidTicket(vehicleNo, date, date)?.let {
                        parkingtype = it.ticketType!!.code
                        validDate = it.expireDate
                        ticketSn = it.sn
                    }?: kotlin.run {
                        // TODO 방문차량 확인
                        if(parkSiteInfoService.isVisitorExternalKeyType()){
                            parkSiteInfoService.getVisitorExternalInfo()?.let { it -> val kaptCode = it["key"]
                                parkinglotService.searchVisitorExternal(it, vehicleNo)?.let {
                                    it.body?.let { body ->
                                        when(body.`object`?.get("isVisitor")){
                                            "Y"->{
                                                parkingtype = TicketType.VISITTICKET.code
                                                memo = body.`object`?.get("purpose").toString()

                                                val dong = body.`object`?.get("dong").toString()
                                                val ho = body.`object`?.get("ho").toString()

                                                // TODO 방문차량 정기권 등록
                                                val requestData = reqCreateProductTicket(
                                                    vehicleNo = vehicleNo,
                                                    ticketType = TicketType.VISITTICKET,
                                                    // TODO 유효기간 설정 우선 당일, 옵션이 필요함
                                                    //gateId = mutableSetOf(parkinglotService.getGateInfoByDtFacilityId(dtFacilitiesId)!!.gateId),
                                                    effectDate = DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate),
                                                    expireDate = DateUtil.lastTimeToLocalDateTime(DateUtil.nowDate),
                                                    etc = dong+"동 " + ho+"호 방문차량",
                                                    etc1 = "아파트너"
                                                )

                                                visitorData = reqVisitorExternal(
                                                    kaptCode = kaptCode,
                                                    carNo = vehicleNo,
                                                    dong = dong,
                                                    ho = ho,
                                                    isResident = "N"
                                                )

                                                productService.createProduct(requestData)

                                            }
                                            else ->{
                                                parkingtype = "NORMAL"
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // 기 입차 여부 확인 및 update
                    val parkins = searchParkInByVehicleNo(vehicleNo, gate.gateId)
                    if (parkins.isNotEmpty()) {
                        parkins.filter { it.outSn == 0L }.forEach {
                            it.outSn = -1
                            parkInService.save(it)
                        }
                    }
                }

                // 입차 정보 DB insert
                val newData = ParkIn(
                    sn = request.inSn?.let { request.inSn } ?: run { null },
                    gateId = gate.gateId,
                    parkcartype = if (request.resultcode.toInt() == 100 || request.resultcode.toInt() == 101) "PARTRECOGNIZED" else parkingtype,
                    userSn = 0,
                    vehicleNo = vehicleNo,
                    image = fileFullPath,
                    flag = 0,
                    validate = validDate,
                    resultcode = resultcode.toInt(),
                    requestid = requestId,
                    fileuploadid = fileUploadId,
                    hour = DateUtil.nowTimeDetail.substring(0, 2),
                    min = DateUtil.nowTimeDetail.substring(3, 5),
                    inDate = date,
                    uuid = uuid,
                    udpssid = if (gate.takeAction == "PCC") "11111" else "00000",
                    ticketSn = ticketSn,
                    memo = memo,
                    date = date.toLocalDate()
                )
                parkInService.saveByParkIn(newData)

                // 시설 I/F
                // PCC 가 아닌경우애만 아래 모듈 실행
                // 1. gate open

                // 2. 전광판
                // 전광판 메세지 구성은 아래와 같이 진행한다.
                // 'pcc' 인 경우 MEMBER -> MEMBER 로 아닌 경우 MEMBER -> NONMEMBER 로 정의

                //긴급차량인 경우 무조건 gate open
                if (request.isEmergency!!) {
                    inFacilityIF(parkingtype!!, vehicleNo, gate.gateId, true, isSecond?: false)
                } else {
                    if (gate.takeAction != "PCC"){
                        val currentOpen = isOpenGate(GateDTO(gate), request.date, newData.parkcartype?: "NORMAL")
                        inFacilityIF(parkingtype!!, vehicleNo, gate.gateId, (!beforeOpen && currentOpen), isSecond?: false)
                    }

                    //todo 아파트너 입차 정보 전송
                    visitorData?.let { data ->
                        parkSiteInfoService.getVisitorExternalInfo()?.let {
                            parkinglotService.sendInVisitorExternal(it, data, parkingtype!!)
                        }
                    }
                }

                logger.warn {" ##### 입차 요청 END #####"}
                return CommonResult.data(newData)
            }
            logger.error("parkIn error failed gateId is not found {} ", dtFacilitiesId )
            return CommonResult.error("parkin failed gateId is not found ")

        } catch (e: RuntimeException) {
            logger.error { "parkin error $e" }
            return CommonResult.error("parkinglot feature db add failed ")
        }
    }

    fun makeParkPhrase(parkingtype: String, vehicleNo: String, text: String? = null, type: String): ArrayList<reqDisplayMessage> {
        val messages = ArrayList<reqDisplayMessage>()
        val lists = when(parkingtype) {
            /* 정기차량 */
            "VIP" -> filterDisplayMessage(type, DisplayMessageType.VIP)
            /* 티맵회원 */
            "MEMBER" -> filterDisplayMessage(type, DisplayMessageType.MEMBER)
            /* 일반차량 */
            "NONMEMBER" -> filterDisplayMessage(type, DisplayMessageType.NONMEMBER)
            /* 번호인식실패 */
            "FAILNUMBER" -> filterDisplayMessage(type, DisplayMessageType.FAILNUMBER)
            /* 입차제한차량 */
            "RESTRICTE" -> filterDisplayMessage(type, DisplayMessageType.RESTRICTE)
            "CALL" -> filterDisplayMessage(type, DisplayMessageType.CALL)
            "FULL" -> filterDisplayMessage(type, DisplayMessageType.FULL)
            "INIT" -> filterDisplayMessage(type, DisplayMessageType.INIT)
            "ERROR" -> filterDisplayMessage(type, DisplayMessageType.ERROR)
            else -> filterDisplayMessage(type, DisplayMessageType.FAILNUMBER)
        }
        lists.forEach { list ->
            val message = reqDisplayMessage(
                order = list.order!!, line = list.lineNumber!!,
                color = list.displayColor!!.colorCode!!,
                text = if (list.messageDesc == "-") text!! else list.messageDesc?: ""
            )
            messages.add(message)
        }
        return messages
    }

    fun filterDisplayMessage(messageClass: String, type: DisplayMessageType): List<DisplayMessageDTO> {
        return when (messageClass) {
            "IN" -> displayService.displayMessagesIn.filter { it.messageType == type }
                .sortedBy { it.order }
            "WAIT" -> displayService.displayMessagesWait.filter { it.messageType == type }
                .sortedBy { it.order }
            else -> displayService.displayMessagesOut.filter { it.messageType == type }
                .sortedBy { it.order }
        }
    }

    fun searchParkInByVehicleNo(vehicleNo: String, gateId: String) : MutableList<ParkInDTO> {
        logger.debug { "VehicleService searchParkInByVehicleNo search param : $vehicleNo $gateId" }
        return parkInQueryService.findByCriteria(ParkInCriteria(vehicleNo = vehicleNo, gateId = gateId, delYn = YN.N))
    }

    fun modifyInOutVehicleByTmap(request: reqInOutVehicleInformationSetup, requestId: String) {
        logger.info{ "modifyInOutVehicle $request" }
        try {
            val inVehicle = parkInRepository.findByUdpssid(request.sessionId)
            when(request.setupOption) {
                SetupOption.ADD -> {
                    if (request.informationType.equals("inVehicle")) {
                        if (inVehicle != null) {
                            inVehicle.parkcartype = "일반차량"
                            inVehicle.vehicleNo = request.vehicleNumber
                            inVehicle.inDate = DateUtil.stringToLocalDateTime(request.inVehicleDateTime)
                            inVehicle.requestid = requestId
                            inVehicle.udpssid = request.sessionId
                            parkInRepository.save(inVehicle)
                        } else {
                            parkInRepository.save(
                                ParkIn(
                                    sn = null, parkcartype = "일반차량",
                                    vehicleNo = request.vehicleNumber,
                                    gateId = "GATE001",
                                    requestid = requestId,
                                    outSn = -100,
                                    udpssid = request.sessionId,
                                    inDate = DateUtil.stringToLocalDateTime(request.inVehicleDateTime)
                                )
                            )
                        }
                    } else {
                        // out
                        if (inVehicle != null) {
                            val outDate = parkOutRepository.findBySn(inVehicle.outSn!!)
                                outDate.ifPresentOrElse(
                                { outVehicle ->
                                    outVehicle.parkcartype = "일반차량"
                                    outVehicle.vehicleNo = request.vehicleNumber
                                    outVehicle.requestid = requestId
                                    outVehicle.outDate = LocalDateTime.now()
                                    outVehicle.parktime = DateUtil.diffMins(
                                        DateUtil.stringToLocalDateTime(request.inVehicleDateTime),
                                        LocalDateTime.now()
                                    )
                                    parkOutRepository.save(outVehicle)
                                    inVehicle.outSn = outVehicle.sn
                                },
                                {
                                    val newParkOut = parkOutRepository.save(
                                        ParkOut(
                                            sn = null, parkcartype = "일반차량",
                                            vehicleNo = request.vehicleNumber,
                                            gateId = "GATE001",
                                            requestid = requestId, outVehicle = 1,
                                            outDate = LocalDateTime.now(),
                                            parktime = DateUtil.diffMins(
                                                DateUtil.stringToLocalDateTime(request.inVehicleDateTime),
                                                LocalDateTime.now()
                                            )
                                        )
                                    )
                                    inVehicle.outSn = newParkOut.sn
                                }
                            )
                            inVehicle.udpssid = request.sessionId
                            parkInRepository.save(inVehicle)
                        }
                    }
                }
                SetupOption.UPDATE -> {
                    if (inVehicle != null) {
                        inVehicle.vehicleNo = request.vehicleNumber
                        inVehicle.inDate = DateUtil.stringToLocalDateTime(request.inVehicleDateTime)
                        inVehicle.requestid = requestId
                        inVehicle.udpssid = request.sessionId
                        parkInRepository.save(inVehicle)
                    }
                }
                SetupOption.DELETE -> {
                    if (inVehicle != null) {
                        inVehicle.delYn = YN.Y
                        inVehicle.requestid = requestId
                        inVehicle.udpssid = request.sessionId
                        parkInRepository.save(inVehicle)
                    }
                }
                else -> {
                    tmapSendService.sendTmapInterface(
                        reqSendResultResponse(result = "FAIL"), parkSiteInfoService.generateRequestId(),
                        "inOutVehicleInformationSetupResponse"
                    )
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "modifyInOutVehicleByTmap failed ${e.message}" }
            tmapSendService.sendTmapInterface(
                reqSendResultResponse(result = "FAIL"), parkSiteInfoService.generateRequestId(),
                "inOutVehicleInformationSetupResponse"
            )
        }
        tmapSendService.sendTmapInterface(
            reqSendResultResponse(result = "SUCCESS"), parkSiteInfoService.generateRequestId(),
            "inOutVehicleInformationSetupResponse"
        )
    }

    fun saveImage(base64Str: String, vehicleNo: String, udpGateid: String) : String {
        val fileFullPath: String = "$imagePath/"+ LocalDate.now()
        File(fileFullPath).apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val fileName = parkSiteInfoService.getParkSiteId()+"_"+udpGateid+"_"+ DateUtil.nowTimeDetail.substring(
            9,
            12
        )+vehicleNo+".jpg"
        val imageByteArray = Base64Util.decodeAsBytes(base64Str)
        if (imageByteArray != null) {
            File("$fileFullPath/$fileName").writeBytes(imageByteArray)
        }
        return "$fileFullPath/$fileName"
    }

//    fun parkOut(request: reqAddParkOut) : CommonResult = with(request){
//        logger.warn{"출차 event car_number: ${request.vehicleNo} out_date: ${request.date} facilityId: ${request.dtFacilitiesId} uuid: ${request.uuid}"}
//        try {
//            if (requestId.isNullOrEmpty()) {
//                requestId = parkSiteInfoService.generateRequestId()
//            }
//
//            // uuid 확인 후 skip
//            parkOutRepository.findByUuid(uuid)?.let {
//                logger.info{ "park out uuid $uuid exists $it "}
//                if (it.parkcartype != "미인식차량" && DataCheckUtil.isValidCarNumber(vehicleNo)) {
//                    logger.error { "park out uuid $uuid exists " }
//                    return CommonResult.exist(request.uuid, "park out uuid exists")
//                }
//                requestId = it.requestid
//                parkOut = it
//                outSn = it.sn
//            }
//
//            parkinglotService.getGateInfoByDtFacilityId(dtFacilitiesId)?.let { gate ->
//                val facility = parkinglotService.getFacilityByDtFacilityId(dtFacilitiesId)
//                // image 파일 저장
//                if (base64Str != null) {
//                    fileFullPath = saveImage(base64Str!!, vehicleNo, gate.udpGateid!!)
//                    fileName = fileFullPath!!.substring(fileFullPath!!.lastIndexOf("/")+1)
//
//                    fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
//                }
//
//                //차량번호 패턴 체크
//                if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
//                    parkingtype = "NORMAL"
//                    //todo 정기권 차량 여부 확인
//                    productService.getValidProductByVehicleNo(vehicleNo, date, date)?.let {
//                        parkingtype = it.ticketType!!.code
//                        validDate = it.validDate
//                    }
//                    recognitionResult = "RECOGNITION"
//
//                    // park-in update
//                    parkInRepository.findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo, 0L, DelYn.N, date)?.let { it ->
//                        logger.warn { "입차 확인 sn car_num ${it.sn} car_num ${it.vehicleNo} " }
//                        parkIn = it
//
//                        parkOutRepository.findTopByInSnAndDelYnOrderByOutDateDesc(it.sn!!, DelYn.N)?.let { exist ->
//                            requestId = exist.requestid
//                            parkOut = exist
//                            outSn = exist.sn
//                            logger.warn { "미출차 확인 sn car_num ${request.outSn} car_num ${exist.vehicleNo} " }
//                        }
//                    }
//
//                    if (parkIn == null && !parkOutRepository.findByVehicleNoEndsWith(vehicleNo).isNullOrEmpty()) {
//                        logger.error { "출차 데이터 입차 내역에 없음 ${request.vehicleNo} " }
//                        //전광판 내역 표기 추가
//                        displayMessage("CALL", vehicleNo, "WAIT", gate.gateId)
//                        // todo 정산기 번호 검색 추가
//                        return CommonResult.exist(request.vehicleNo, "출차 데이터 입차 내역에 없음 ${request.vehicleNo}")
//                    }
//
//                } else {
//                    parkingtype = "UNRECOGNIZED"
//                    recognitionResult = "NOTRECOGNITION"
//                }
//
//                displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
//
//                if (parkSiteInfoService.parkSite!!.saleType == SaleType.PAID && parkIn != null) {
//                    price = feeCalculation.getBasicPayment(parkIn!!.inDate!!, date, VehicleType.SMALL, vehicleNo, 1, 0, parkIn!!.sn)
//                    logger.warn { "-------------------getBasicPayment Result -------------------" }
//                    logger.warn { "입차시간 : $parkIn!!.inDate!! / 출차시간 : $date / 주차시간: ${price!!.parkTime}" }
//                    logger.warn { "총 요금 : ${price!!.orgTotalPrice} / 결제 요금 : ${price!!.totalPrice} / 할인 요금 : ${price!!.discountPrice} / 일최대할인요금 : ${price!!.dayilyMaxDiscount}" }
//                }
//
////                logger.warn { "미출차 확인 sn car_num ${request.outSn} car_num ${vehicleNo} " }
//
//                // 출차 정보 DB insert
//                val newData = ParkOut(
//                    sn = request.outSn?.let { request.outSn }?: kotlin.run { null },
//                    gateId = gate.gateId, //parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateId,
//                    parkcartype = parkingtype,
//                    userSn = 0,
//                    vehicleNo = vehicleNo,
//                    image = "$fileFullPath",
//                    flag = 0,
//                    validate = validDate,
//                    resultcode = resultcode.toInt(),
//                    requestid = requestId,
//                    fileuploadid = fileUploadId,
//                    hour = DateUtil.nowTimeDetail.substring(0, 2),
//                    min = DateUtil.nowTimeDetail.substring(3, 5),
//                    outDate = date,
//                    uuid = uuid,
//                    parktime = if (price == null) parkIn?.let { DateUtil.diffMins(parkIn!!.inDate!!, date) }?: kotlin.run { 0 } else price!!.parkTime,
//                    parkfee = if (price == null) null else price!!.orgTotalPrice,
//                    payfee = if (price == null) null else price!!.totalPrice,
//                    discountfee = if (price == null) null else price!!.discountPrice,
//                    dayDiscountfee = if (price == null) null else price!!.dayilyMaxDiscount,
//                    inSn = parkIn?.sn ?: kotlin.run { null },
//                    date = date.toLocalDate()
//                )
//                parkOutRepository.saveAndFlush(newData)
//
//                // tmap 연동
//                if (parkSiteInfoService.isTmapSend()) {
//                    when (parkingtype) {
//                        "SEASONTICKET", "WHITELIST" -> tmapSendService.sendOutVehicle(
//                            reqOutVehicle(
//                                gateId = gate.udpGateid!!,
//                                seasonTicketYn = "Y",
//                                vehicleNumber = vehicleNo,
//                                recognitionType = facility!!.category!!,
//                                recognitorResult = recognitionResult!!,
//                                fileUploadId = fileUploadId!! ),
//                            requestId!!, fileName)
//                        "NORMAL" -> {
//                            tmapSendService.sendAdjustmentRequest(
//                                reqAdjustmentRequest(
//                                    gateId = gate.udpGateid!!,
//                                    paymentMachineType = "exit",
//                                    vehicleNumber = vehicleNo,
//                                    recognitionType = facility!!.category!!.toString(),
//                                    facilitiesId = parkFacilityRepository.findByGateIdAndCategory(gate.gateId, FacilityCategoryType.PAYSTATION)?.get(0)!!.facilitiesId!!,
//                                    fileuploadId = fileUploadId!!
//                                ),
//                                requestId!!
//                            )
//                            vehicleListSearchRepository.save(VehicleListSearch(requestId, parkFacilityRepository.findByGateIdAndCategory(gate.gateId, FacilityCategoryType.PAYSTATION)?.get(0)!!.facilitiesId!!))
//                        }
//                    }
//                }
//
//                if (deviceIF == "ON") {
//
//                    // 정산기 출차 전송
//                    when (parkingtype) {
//                        "UNRECOGNIZED" -> {
//                            facilityService.sendPaystation(
//                                reqPayStationData(
//                                    paymentMachineType = "EXIT",
//                                    vehicleNumber = vehicleNo,
//                                    facilitiesId = gate.udpGateid!!,
//                                    recognitionType = "NOTRECOGNITION",
//                                    recognitionResult = recognitionResult!!,
//                                    paymentAmount = "0",
//                                    vehicleIntime = DateUtil.nowDateTimeHm
//                                ),
//                                gate = gate.gateId,
//                                requestId = requestId!!,
//                                type = "adjustmentRequest"
//                            )
//                            displayMessage(parkingtype!!, vehicleNo, "WAIT", gate.gateId)
//                        }
//                        else -> {
//                            if (parkSiteInfoService.parkSite!!.saleType == SaleType.PAID) {
//                                displayMessage(
//                                    parkingtype!!,
//                                    if (price != null) (price!!.totalPrice).toString() + "원" else "0원",
//                                    "WAIT",
//                                    gate.gateId
//                                )
//
//                                price?.let { price ->
//                                    facilityService.sendPaystation(
//                                        reqPayStationData(
//                                            paymentMachineType = if (parkingtype == "NORMAL") "exit" else if (price.totalPrice!! > 0) "exit" else "SEASON",
//                                            vehicleNumber = vehicleNo,
//                                            facilitiesId = gate.udpGateid ?: kotlin.run { gate.gateId },
//                                            recognitionType = if (parkingtype == "NORMAL") "FREE" else if (price.totalPrice!! > 0) "FREE" else "SEASON",
//                                            recognitionResult = "RECOGNITION",
//                                            paymentAmount = if (price.totalPrice!! <= 0) "0" else (price.orgTotalPrice).toString(),
//                                            parktime = price.parkTime.toString(),
//                                            parkTicketMoney = if (price.totalPrice!! <= 0) "0" else (price.discountPrice!! + price.dayilyMaxDiscount!!).toString(),  // 할인요금
//                                            vehicleIntime = parkIn?.let {
//                                                DateUtil.formatDateTime(
//                                                    it.inDate!!,
//                                                    "yyyy-MM-dd HH:mm"
//                                                )
//                                            } ?: kotlin.run { DateUtil.nowDateTimeHm }
//                                        ),
//                                        gate = gate.gateId,
//                                        requestId = newData.sn.toString(),
//                                        type = "adjustmentRequest"
//                                    )
//
//                                    if (price.totalPrice == 0) {
//                                        //parkIn?.let { updateParkInExitComplete(it, newData.sn!! ) }
//                                        //displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
//                                        logger.warn { "parkout car_number: ${request.vehicleNo} 출차 gate ${gate.gateId} open" }
//                                        //relayService.actionGate(gate.gateId, "GATE", "open")
//                                        relayClient.sendActionBreaker(gate.gateId, "open")
//                                    }
//                                }
//                            } else {
//                                logger.warn { "parkout car_number: ${request.vehicleNo} 출차 gate ${gate.gateId} open" }
//                                //relayService.actionGate(gate.gateId, "GATE", "open")
//                                relayClient.sendActionBreaker(gate.gateId, "open")
//                            }
//                        }
//                    }
//                    // 동일 입차 출차 처리
//                    parkIn?.let { updateParkInExitComplete(parkInMapper.toDTO(it), newData.sn!! ) }
//                    // 전광판 display 전송
////                    if (parkinglotService.parkSite!!.saleType == SaleType.FREE) {
////                        //parkIn?.let { updateParkInExitComplete(it, newData.sn!! ) }
////                        //displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
////                        logger.warn { "parkout car_number: ${request.vehicleNo} 출차 gate ${gate.gateId} open" }
////                        relayService.actionGate(gate.gateId, "GATE", "open")
////                    } else {
////                        //todo 출구 제한 삭제
//////                        if (gate.openAction == OpenActionType.NONE && (price!!.totalPrice == 0)) {
////                        if (price!!.totalPrice == 0) {
////                            //parkIn?.let { updateParkInExitComplete(it, newData.sn!! ) }
////                            //displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
////                            logger.warn { "parkout car_number: ${request.vehicleNo} 출차 gate ${gate.gateId} open" }
////                            relayService.actionGate(gate.gateId, "GATE", "open")
////                        }
////                    }
//                }
//
//                logger.warn { "parkout car_number: ${request.vehicleNo} 출차 성공" }
//                return CommonResult.created()
//            }
//            logger.error { "parkout car_number: ${request.vehicleNo} 출차 failed" }
//            return CommonResult.error("parkout car_number: ${request.vehicleNo} 출차 failed ")
//        } catch (e: CustomException) {
//            logger.error { "parkout car_number: ${request.vehicleNo} 출차 failed $e" }
//            return CommonResult.error("parkout add failed ")
//        }
//    }

    fun updateParkInExitComplete(data: ParkInDTO, outSn: Long) : Boolean {
        try {
            data.outSn = outSn
            parkInService.save(data)

            // 동일 UUID 에 대해서 del_ny 처리
            data.uuid?.let { inUuid ->
                parkInRepository.findByUuidAndOutSnAndDelYn(inUuid, 0, YN.N)?.let { ins ->
                    ins.forEach {
                        it.delYn = YN.Y
                        parkInRepository.save(it)
                        parkInRepository.flush()
                    }
                }
            }
            return true
        } catch (e: CustomException) {
            logger.error { "tb_parkin car_number: ${data.vehicleNo} out_sn update failed $e" }
            return false
        }
    }

    fun updateInoutPaymentExitComplete(data: InoutPaymentDTO, outSn: Long): Boolean {
        data.outSn = outSn
        inoutPaymentService.save(data)
        return true
    }

    @Transactional(readOnly = true)
    open fun getInout(sn: Long): resParkInList {
        logger.debug { "getInout $sn" }
        parkInQueryService.findByCriteria(ParkInCriteria(sn = sn))[0].let { parkInDTO ->
            val result = resParkInList(
                type = DisplayMessageClass.IN,
                parkinSn = parkInDTO.sn!!, vehicleNo = parkInDTO.vehicleNo, parkcartype = parkInDTO.parkcartype!!,
                inGateId = parkInDTO.gateId, inDate = parkInDTO.inDate!!,
                ticketCorpName = parkInDTO.seasonTicketDTO?.corpName, memo = parkInDTO.memo,
                inImgBase64Str = parkInDTO.image?.let { image -> image.substring(image.indexOf("/park")) },
                parkoutSn = parkInDTO.outSn
            )
            result.paymentAmount = inoutPaymentRepository.findByInSnAndResultAndDelYn(parkInDTO.sn!!, ResultType.SUCCESS, YN.N)?.let { payment ->
                payment.sumBy { it.amount?: 0 }
            }?: kotlin.run { 0 }

            result.aplyDiscountClasses = discountService.searchInoutDiscount(parkInDTO.sn!!) as ArrayList<InoutDiscount>?
//                            if (it.outSn!! > 0L && it.outSn != null) {
            parkOutRepository.findByInSnAndDelYn(parkInDTO.sn!!, YN.N).ifPresent { out ->
                result.type = DisplayMessageClass.OUT
                result.parkoutSn = out.sn
                result.outDate = out.outDate
                result.outGateId = out.gateId
                result.parktime = out.parktime
                result.parkfee = out.parkfee
                result.payfee = out.payfee?: 0
                result.discountfee = out.discountfee
                result.dayDiscountfee = out.dayDiscountfee
                result.outImgBase64Str = out.image?.let { if (out.image!!.contains("/park")) out.image!!.substring(out.image!!.indexOf("/park")) else null }?: kotlin.run { null }
                result.nonPayment = result.payfee!! - result.paymentAmount!!
            }
            return result
        }
    }


    @Transactional(readOnly = true)
    open fun getAllParkLists(request: reqSearchParkin): List<resParkInList>? {
        logger.info { "getAllParkLists $request" }
        try {
            when (request.searchDateLabel) {
                DisplayMessageClass.IN -> {
                    val results = ArrayList<resParkInList>()
                    val criteria = ParkInCriteria(
                        sn = if (request.searchLabel == "INSN" && request.searchText != null) request.searchText!!.toLong() else null,
                        vehicleNo = if (request.searchLabel == "CARNUM" && request.searchText != null) request.searchText!! else null,
                        fromDate = request.fromDate,
                        toDate = request.toDate,
                        gateId = request.gateId,
                        parkcartype = request.parkcartype,
                        outSn = request.outSn
                    )
                    parkInQueryService.findByCriteria(criteria).let { list ->
                        list.forEach { it ->
                            val result = resParkInList(
                                type = if ((it.outSn?: -1) > 0) DisplayMessageClass.OUT else DisplayMessageClass.IN,
                                parkinSn = it.sn!!, vehicleNo = it.vehicleNo, parkcartype = it.parkcartype!!,
                                inGateId = it.gateId, inDate = it.inDate!!,
                                ticketCorpName = it.seasonTicketDTO?.corpName, memo = it.memo,
                                inImgBase64Str = it.image?.let { image -> image.substring(image.indexOf("/park")) },
                                parkoutSn = it.outSn
                            )
                            result.paymentAmount = inoutPaymentRepository.findByInSnAndResultAndDelYn(it.sn!!, ResultType.SUCCESS, YN.N)?.let { payment ->
                                payment.sumBy { it.amount?: 0 }
                            }?: kotlin.run { 0 }
//
//                            result.aplyDiscountClasses = discountService.searchInoutDiscount(it.sn!!) as ArrayList<InoutDiscount>?
//                            if (it.outSn!! > 0L && it.outSn != null) {
                            parkOutRepository.findByInSnAndDelYn(it.sn!!, YN.N).ifPresent { out ->
                                result.parkoutSn = out.sn
                                result.outDate = out.outDate
                                result.outGateId = out.gateId
                                result.parktime = out.parktime
                                result.parkfee = out.parkfee
                                result.payfee = out.payfee?: 0
                                result.discountfee = out.discountfee
                                result.dayDiscountfee = out.dayDiscountfee
                                result.outImgBase64Str = out.image?.let { if (out.image!!.contains("/park")) out.image!!.substring(out.image!!.indexOf("/park")) else null }?: kotlin.run { null }
                                result.nonPayment = result.payfee!! - result.paymentAmount!!
                            }
                            results.add(result)
                        }
                    }
                    results.sortedByDescending { it.inDate }
                    return results
                }
                DisplayMessageClass.OUT -> {
                    val results = ArrayList<resParkInList>()
                    val criteria = ParkOutCriteria(
                        sn = if (request.searchLabel == "INSN" && request.searchText != null) request.searchText!!.toLong() else null,
                        vehicleNo = if (request.searchLabel == "CARNUM" && request.searchText != null) request.searchText!! else null,
                        fromDate = request.fromDate,
                        toDate = request.toDate,
                        gateId = request.gateId,
                        parkcartype = request.parkcartype

                    )

                    parkOutQueryService.findByCriteria(criteria).let { list ->
                        list.filter { it.inSn != null }.forEach {
                            var paymentAmount = inoutPaymentRepository.findByInSnAndResultAndDelYn(it.parkInDTO?.sn!!, ResultType.SUCCESS, YN.N)?.let { payment -> payment.sumBy { it.amount?: 0 } }?: kotlin.run { 0 }
                            results.add(
                                resParkInList(
                                    type = DisplayMessageClass.OUT,
                                    parkinSn = it.parkInDTO?.sn!!,
                                    vehicleNo = it.parkInDTO!!.vehicleNo,
                                    parkcartype = it.parkInDTO!!.parkcartype!!,
                                    inGateId = it.parkInDTO!!.gateId,
                                    inDate = it.parkInDTO!!.inDate!!,
                                    ticketCorpName = it.parkInDTO!!.seasonTicketDTO?.corp?.corpName,
                                    parkoutSn = it.sn ,
                                    outDate = it.outDate,
                                    outGateId = it.gateId,
                                    parktime = it.parktime,
                                    parkfee = it.parkfee, payfee = it.payfee, discountfee = it.discountfee,
                                    paymentAmount = paymentAmount,
                                    nonPayment = it.payfee!! - paymentAmount
//                                    aplyDiscountClasses = discountService.searchInoutDiscount(it.parkInDTO?.sn!!) as ArrayList<InoutDiscount>?
                                )
                            )
                        }
                    }
                    results.sortedByDescending { it.outDate }
                    return results
                }
                else -> return null
            }
        } catch (e: RuntimeException) {
            logger.error { "getAllParkLists $e" }
            return null
        }
    }

    fun adjustmentRequestResponse(request: reqAdjustmentRequestResponse, requestId: String) {
        // 5s skip
        parkOutRepository.findByRequestid(requestId)?.let {  it ->
            if (DateUtil.diffSecs(it.UpdateDate!!, LocalDateTime.now()) <= 5) {
                when(request.result) {
                    "SUCCESS" -> {
                        // open Gate
                        if (request.outVehicleAllowYn == "Y" || request.adjustmentAmount == 0) {
                            //relayService.actionGate(it.gateId!!, "GATE", "open")
                            relayClient.sendActionBreaker(it.gateId ?: "", "open")

                            facilityService.sendPaystation(
                                reqPayStationData(paymentMachineType = "exit",
                                    vehicleNumber = it.vehicleNo!!,
                                    facilitiesId = "GLNT00001",
                                    recognitionType = if (request.allowYnReason == "SEASONTICKET") "SEASON" else "FREE",
                                    recognitionResult = "RECOGNITION",
                                    paymentAmount = "0",
                                    vehicleIntime = DateUtil.stringToLocalDateTime(request.inVehicleDateTime).toString() ),
                                gate = it.gateId!!,
                                requestId = it.sn!!.toString(),
                                type = "adjustmentRequest")
                            displayMessage(
                                if (request.allowYnReason == "SEASONTICKET") "정기차량" else "MEMBER",
                                request.adjustmentAmount.toString()+"원", "WAIT", it.gateId!!)
                        } else {
                            facilityService.sendPaystation(
                                reqPayStationData(paymentMachineType = "exit",
                                    vehicleNumber = it.vehicleNo!!,
                                    facilitiesId = "GLNT00001",
                                    recognitionType = "FREE",
                                    recognitionResult = "RECOGNITION",
                                    paymentAmount = request.parkingAmount.toString(),
                                    parktime = request.chargingTimes.toString(),
                                    adjustmentDateTime = DateUtil.stringToLocalDateTime(request.chargingRequestDateTime).toString(),
                                    vehicleIntime = DateUtil.stringToLocalDateTime(request.inVehicleDateTime).toString() ),
                                gate = it.gateId!!,
                                requestId = it.sn!!.toString(),
                                type = "adjustmentRequest")

                            facilityService.sendPaystation(
                                reqPayData(paymentMachineType = "beforehand",
                                    vehicleNumber = it.vehicleNo!!,
                                    parkTicketType = "OK",
                                    parkTicketMoney = (request.parkingAmount - request.adjustmentAmount).toString() ),
                                gate = it.gateId!!,
                                requestId = it.sn!!.toString(),
                                type = "adjustmentdataRequest")

                            displayMessage(
                                "MEMBER",
                                request.adjustmentAmount.toString()+"원", "WAIT", it.gateId!!)

                            it.parktime = request.chargingTimes!!.toInt()
                            it.parkfee = request.chargingAmount
                            it.chargingId = request.chargingId
                            parkOutRepository.save(it)
                            parkOutRepository.flush()
                        }
                    }
                    "FAILED" -> {
                        // open gate
//                        relayService.actionGate(it.gateId!!, "GATE", "open")
                        relayClient.sendActionBreaker(it.gateId ?: "", "open")
                        it.outVehicle = 1
                        // todo 정산기 I/F
                        // todo 전광판
                        // tmap outVehicle
                        // 파일 업로드
                    }
                }
            }
        }
    }

    fun updatePayment(request: reqUpdatePayment): ParkOut? = with(request){
        var parkOut: ParkOut? = null
        parkOutRepository.findBySn(request.sn).ifPresentOrElse(
            { it ->
                it.discountfee = parkTicketAmount
                it.payfee = paymentAmount
                it.cardtransactionid = cardtransactionId
//            it.parkcartype = parkcarType
                it.cardNumber = cardNumber!!.substring(4)
                it.outVehicle = 1
                it.approveDatetime = approveDateTime

                displayMessage(it.parkcartype!!, it.vehicleNo!!, "OUT", it.gateId!!)
                parkOut = parkOutRepository.save(it)
            },
            {
                logger.error("payment park-out update $sn not found")
            }
        )
        return parkOut
    }

    fun displayMessage(parkingtype: String, vehicleNo: String, type: String, gateId: String) {
        // reset 여부 판
        var reset: String = "on"
        if (type == "WAIT" && vehicleNo.contains("원")) reset = "off"

        val displayMessage = when (parkingtype) {
            "NORMAL" -> makeParkPhrase("NONMEMBER", vehicleNo, vehicleNo, type)
            "UNRECOGNIZED" -> {
                when (type) {
                    "IN" -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
                    "OUT" -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
                    else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
                }
            }
            "SEASONTICKET", "WHITELIST", "FREETICKET", "VISITTICKET" -> {
                val days = ticketService.calcRemainDayTicket(vehicleNo)
                if (parkingtype == "SEASONTICKET" && days in 0..7)
                    makeParkPhrase("VIP", vehicleNo, "잔여 0${days}일", type)
                else
                    makeParkPhrase("VIP", vehicleNo, vehicleNo, type)
            }
            "MEMBER", "RESTRICTE", "FULL", "INIT", "CALL", "ERROR" -> makeParkPhrase(parkingtype, vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("RESTRICTE", vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("FULL", vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("INIT", vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("")
            else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
        }
        relayClient.sendShowDisplayMessages(gateId, type, displayMessage, reset)
//        relayService.sendDisplayMessage(displayMessage, gateId, reset, type)
    }

    fun lastSettleData(facilityId: String): ParkOut? {
        return parkOutRepository.findTopByPaystationOrderByOutDateDesc(facilityId)
    }

    @Transactional(readOnly = true)
    open fun getParkInOutDetail(request: Long): CommonResult {
        logger.info { "getParkInOutDetail inseq $request" }
        try {
            parkInRepository.findBySn(request)?.let { it ->
                val result = resParkInList(
                    type = DisplayMessageClass.IN,
                    parkinSn = it.sn!!, vehicleNo = it.vehicleNo, parkcartype = it.parkcartype!!,
                    inGateId = it.gateId, inDate = it.inDate!!, inImgBase64Str = it.image!!.substring(it.image!!.indexOf("/park")),
                    memo = it.memo
                )
                if (it.outSn!! > 0L || it.outSn != null) {
                    parkOutRepository.findBySn(it.outSn!!).ifPresent { out ->
                        result.type = DisplayMessageClass.OUT
                        result.parkoutSn = out.sn
                        result.outDate = out.outDate
                        result.outGateId = out.gateId
                        result.parktime = out.parktime
                        result.parkfee = out.parkfee
                        result.payfee = out.payfee
                        result.discountfee = out.discountfee
                    }
                }
                return CommonResult.data(result)
            }
        }catch (e: RuntimeException){
            logger.error { "getParkInOutDetail failed ${e.message}" }
            return CommonResult.error("getParkInOutDetail inseq $request failed")
        }
        return CommonResult.error("getParkInOutDetail inseq $request failed")
    }

    @Throws(CustomException::class)
    fun createInout(request: resParkInList): CommonResult {
        logger.info { "createInout request $request" }
        try {
            val gate = gateService.findOne(request.inGateId ?: "")

            gate?.let { gateDTO ->
                val uuid = UUID.randomUUID().toString()
                if (parkInRepository.findByVehicleNoEndsWithAndOutSnAndGateIdAndDelYn(request.vehicleNo?: "", 0, gateDTO.gateId!!, YN.N)!!.isNotEmpty()) {
                    logger.warn{" 기 입차 car_num:${request.vehicleNo} skip "}
                    return CommonResult.data()
                }

                // 기 입차 여부 확인 및 update
                searchParkInByVehicleNo(request.vehicleNo?: "", gate.gateId!!).let { parkins ->
                    if (parkins.isNotEmpty()) {
                        parkins.filter { it.outSn == 0L }.forEach {
                            it.outSn = -1
                            parkInService.save(it)
                        }
                    }
                }

                val parkCarType = confirmParkCarType(request.vehicleNo ?: "", request.inDate, "IN")
                //val recognitionResult = if (parkCarType["parkCarType"] == "UNRECOGNIZED") "NOTRECOGNITION" else "RECOGNITION"

                // 입차 정보 DB insert
                val new = ParkIn(
                    sn = null,
                    gateId = gate.gateId,
                    parkcartype = parkCarType["parkCarType"] as String?,
                    userSn = 0,
                    vehicleNo = request.vehicleNo ?: "",
                    image = null,
                    flag = 0,
                    resultcode = 0,
                    requestid = parkSiteInfoService.generateRequestId(),
                    hour = DateUtil.nowTimeDetail.substring(0, 2),
                    min = DateUtil.nowTimeDetail.substring(3, 5),
                    inDate = request.inDate,
                    uuid = uuid,
                    udpssid = if (gate.takeAction == "PCC") "11111" else "00000",
                    ticketSn = parkCarType["ticketSn"] as Long?,
                    memo = request.memo,
                    date = request.inDate.toLocalDate()
                )
                parkInRepository.saveAndFlush(new)
                logger.warn { "### 수동입차등록 차량번호 ${new.vehicleNo} 입차일시 ${new.inDate} 차량타입 ${new.parkcartype}" }
                return CommonResult.data(new)
            }

            return CommonResult.data()
        }catch (e: RuntimeException){
            logger.error { "createInout failed $e" }
            return CommonResult.error("createInout failed")
        }
    }

    @Throws(CustomException::class)
    fun updateInout(request: resParkInList): resParkInList? {
        logger.info { "updateInout request $request" }
        try {
            parkInQueryService.findByCriteria(ParkInCriteria(sn = request.parkinSn)).let { parkIns ->
                parkIns.forEach { parkIn ->
                    parkIn.inDate = request.inDate
                    parkIn.gateId = request.inGateId?: parkIn.gateId
                    parkIn.vehicleNo = request.vehicleNo?: parkIn.vehicleNo
                    parkIn.parkcartype = request.parkcartype
                    parkIn.memo = request.memo?: parkIn.memo

                    parkInService.save(parkIn)

                    // 할인권 정보 추가
                    request.addDiscountClasses?.let { it ->
                        discountService.addInoutDiscount(it)
                    }

                    request.outDate?.let {
                        if ((request.parkoutSn?: 0) > 0) {
                            request.parkoutSn?.let {
                                //기존 출차 데이터 update
                                parkOutRepository.findBySn(it).ifPresent { parkOut ->
                                    parkOut.outDate = request.outDate
                                    parkOut.parktime = request.parktime
                                    request.outGateId?.let { parkOut.gateId = request.outGateId }
                                    request.parktime?.let { parkOut.parktime = request.parktime }
                                    request.parkfee?.let { parkOut.parkfee = request.parkfee }
                                    request.payfee?.let { parkOut.payfee = request.payfee }
                                    request.dayDiscountfee?.let { parkOut.dayDiscountfee = request.dayDiscountfee }
                                    request.discountfee?.let { parkOut.discountfee = request.discountfee }
                                    parkOutRepository.saveAndFlush(parkOut)
                                }
                            }?: run {
                                return null
                            }
                        } else {
                            // 입차 기준으로 출차 데이터 확인
                            val existIn = request.parkinSn?.let { it1 -> parkOutRepository.findByInSnAndDelYn(it1, YN.N).orElse(null) }
                            val sn = existIn?.sn

                            //신규 출차 데이터
                            val new = ParkOut(
                                sn = sn,
                                gateId = request.outGateId,
                                parkcartype = parkIn.parkcartype,
                                userSn = 0,
                                vehicleNo = request.vehicleNo,
                                flag = 0,
                                resultcode = 4,
                                requestid = parkSiteInfoService.generateRequestId(),
                                hour = DateUtil.nowTimeDetail.substring(0, 2),
                                min = DateUtil.nowTimeDetail.substring(3, 5),
                                outDate = request.outDate,
                                parktime = request.parktime,
                                parkfee = request.parkfee,
                                payfee = request.payfee,
                                discountfee = request.discountfee,
                                dayDiscountfee = request.dayDiscountfee,
                                inSn = parkIn.sn,
                                uuid = UUID.randomUUID().toString(),
                                image = "null"
                            )
                            parkOutRepository.saveAndFlush(new)
                            request.parkoutSn = new.sn
                        }
                    }
                }
            }
            return request
        }catch (e: RuntimeException){
            logger.error { "updateInout failed ${e.message}" }
            return null
        }
    }

    fun deleteInout(sn: Long) : CommonResult {
        try{
            parkInRepository.findBySn(sn)?.let { parkIn ->
                parkIn.delYn = YN.Y
                parkInRepository.save(parkIn)
                parkInRepository.flush()
                parkIn.outSn?.let { outSn ->
                    parkOutRepository.findBySn(outSn).ifPresent { parkOut ->
                        parkOut.delYn = YN.Y
                        parkOutRepository.save(parkOut)
                        parkOutRepository.flush()
                    }
                }
            }
            return CommonResult.data()
        }catch (e: RuntimeException){
            logger.error { "deleteInout failed $e" }
            return CommonResult.error("deleteInout failed")
        }
    }

    fun calcInout(request: resParkInList): resParkInList {
        if (parkSiteInfoService.parkSite!!.saleType == SaleType.PAID) {
            val price = feeCalculation.getCalcPayment(CalcType.CALC, request.inDate, request.outDate, VehicleType.SMALL, request.vehicleNo?: "",  0, request.parkinSn, request.addDiscountClasses, false)
            logger.warn { "-------------------getCalcPayment Result -------------------" }
            logger.warn { "입차시간 : ${request.inDate} / 출차시간 : ${request.outDate} / 주차시간: ${price!!.parkTime}" }
            logger.warn { "총 요금 : ${price!!.orgTotalPrice} / 결제 요금 : ${price.totalPrice} / 할인 요금 : ${price.discountPrice} / 일최대할인요금 : ${price.dayilyMaxDiscount}" }
            request.parktime = price!!.parkTime
            request.parkfee = price.orgTotalPrice
            request.payfee = price.totalPrice
            request.discountfee = price.discountPrice
            request.dayDiscountfee = price.dayilyMaxDiscount
        } else {
            request.paymentAmount = 0
            request.dayDiscountfee = 0
            request.parktime = DateUtil.diffMins(request.inDate, request.outDate!!)
            request.parkfee = 0
            request.payfee = 0
            request.discountfee = 0
        }
        return request
    }

    fun calcParkFee(type: String, inDate: LocalDateTime, outDate: LocalDateTime, vehicleType: VehicleType, vehicleNo: String, parkInSn: Long, discountClasses: ArrayList<ReqAddParkingDiscount>? = null): BasicPrice? {
        val price =
            if (type == "RECALC")
                feeCalculation.getCalcPayment(CalcType.SETTLE, inDate, outDate, vehicleType, vehicleNo, 0, parkInSn, null, true)
            else
                feeCalculation.getCalcPayment(CalcType.SETTLE, inDate, outDate, vehicleType, vehicleNo, 0, parkInSn, discountClasses, false)

        logger.warn { "-------------------getCalcPayment Result -------------------" }
        logger.warn { "입차시간 : $inDate / 출차시간 : $outDate / 주차시간: ${price!!.parkTime}" }
        logger.warn { "총 요금 : ${price!!.orgTotalPrice} / 결제 요금 : ${price.totalPrice} / 할인 요금 : ${price.discountPrice} / 일최대할인요금 : ${price.dayilyMaxDiscount}" }
        return price
    }

    fun savePayment(contents: reqPaymentResult, sn: Long, facilityId: String, outSn: Long? = null): InoutPaymentDTO? {
        logger.debug { "savePayment $sn $contents $outSn" }

        val inoutPayment = inoutPaymentService.findOne(sn).orElse(null)
        inoutPayment?.let { inoutPaymentDTO ->
            inoutPaymentDTO.approveDateTime = contents.approveDatetime
            inoutPaymentDTO.payType = PayType.CARD
            inoutPaymentDTO.amount = contents.cardAmount?.toInt() ?: kotlin.run { null }
            inoutPaymentDTO.cardCorp = contents.cardCorp
            inoutPaymentDTO.cardNumber = contents.cardNumber
            inoutPaymentDTO.transactionId = contents.transactionId
            inoutPaymentDTO.result = contents.result?: ResultType.SUCCESS
            inoutPaymentDTO.failureMessage = contents.failureMessage
            inoutPaymentDTO.outSn = outSn
            inoutPaymentDTO.facilityId = facilityId

            // 결제 금액 저장
            return inoutPaymentService.save(inoutPaymentDTO)
        }
        return null
    }

    fun getImagePath(imagePath: String?): String? {
        return if (imagePath != null && imagePath.contains("/park", true)) { imagePath.substring(imagePath.indexOf("/park")).replace("//", "/") }
            else null
    }

    fun getLastInout(type: GateTypeStatus, gateId: String ): HashMap<String, Any?>? {
        try {
            var result = HashMap<String, Any?>()
            var parkIn = parkInRepository.findTopByGateIdAndDelYnOrderByInDateDesc(gateId, YN.N)
            var parkOut = parkOutRepository.findTopByGateIdAndDelYnOrderByOutDateDesc(gateId, YN.N)

            when (type) {
                GateTypeStatus.IN -> {
                    parkIn?.let {
                        result =
                            hashMapOf<String, Any?>(
                                "gateId" to gateId,
                                "vehicleNo" to it.vehicleNo,
                                "date" to it.inDate,
                                "carType" to it.parkcartype,
                                "image" to getImagePath(it.image)
                            )
                    }
                }
                GateTypeStatus.OUT -> {
                    parkOut?.let {
                        result =
                            hashMapOf<String, Any?>(
                                "gateId" to gateId,
                                "vehicleNo" to it.vehicleNo,
                                "date" to it.outDate,
                                "carType" to it.parkcartype,
                                "image" to getImagePath(it.image)
                            )
                    }
                }
                else -> {
                    result =
                        hashMapOf<String, Any?>(
                            "gateId" to gateId,
                            "vehicleNo" to (parkIn?.vehicleNo ?: ""),
                            "date" to (parkIn?.inDate ?: ""),
                            "carType" to (parkIn?.parkcartype ?: ""),
                            "image" to getImagePath((parkIn?.image ?: "")),
                            "outVehicleNo" to (parkOut?.vehicleNo ?: ""),
                            "outDate" to (parkOut?.outDate ?: ""),
                            "outCarType" to (parkOut?.parkcartype ?: ""),
                            "outImage" to getImagePath((parkOut?.image ?: ""))
                        )
                    }
            }
            return result
        }catch (e: CustomException) {
            logger.error { "getLastInout failed ${e.message}" }
            return null
        }
    }

    // gate별 입차 차량 count
    fun countParkInByGatesAndVehicleStatus(gates: List<String>, status: String): Int {
        return when(status) {
            "IN" -> parkInRepository.countByGateIdInAndOutSn(gates, 0)
            "OUT" -> parkInRepository.countByGateIdInAndOutSnGreaterThan(gates, 0)
            else -> parkInRepository.countByGateIdInAndOutSnGreaterThan(gates, -1)
        }
    }


    fun inFacilityIF(parkCarType: String, vehicleNo: String, gateId: String, isOpen: Boolean, isSecond: Boolean) {
        logger.warn { "## 입차 시설 연계 차량번호: $vehicleNo 입차 gate $gateId $parkCarType 오픈 $isOpen ##" }

        if (parkCarType == "RESTRICTE" || parkCarType == "FULL") {
            logger.warn { "입차 차단 차량: $vehicleNo $parkCarType" }
            displayMessage(parkCarType, vehicleNo, "IN", gateId)
        } else {
            displayMessage(parkCarType, vehicleNo, "IN", gateId)
            if (isOpen) {
                relayClient.sendActionBreaker(gateId, "open")
            }
        }
    }

    fun outFacilityIF(parkCarType: String, vehicleNo: String, gate: Gate, parkIn: ParkIn?, parkOutSn: Long) {
        logger.warn { "parkout car_number: $vehicleNo 출차 gate ${gate.gateId} open" }

        // parkIn update
        parkIn?.let { it ->
            inoutPaymentService.findByInSnAndResult(it.sn ?: -1, ResultType.SUCCESS)?.let { payments ->
                payments.forEach { payment ->
                    updateInoutPaymentExitComplete(payment, parkOutSn)
                }
            }
            if (parkCarType != "FAILURE") {
                updateParkInExitComplete(parkInMapper.toDTO(parkIn), parkOutSn)
            }
        }

        // 출차 메세지
        displayMessage(parkCarType, vehicleNo, "OUT", gate.gateId)
        // gate open
        if (parkCarType != "FAILURE") {
            relayClient.sendActionBreaker(gate.gateId, "open")
        }
    }

    fun waitFacilityIF(type: String, parkCarType: String, vehicleNo: String, gate: Gate, parkOutDTO: ParkOutDTO, inDate: LocalDateTime, dtFacilityId: String? = null, ticketInfo: TicketInfoDTO? = null) {
        // 결제금액 전광판
        val payFee = if (type == "PAYMENT") parkOutDTO.originPayFee?: 0 else parkOutDTO.payfee?: 0
        val discount = if (type == "PAYMENT") parkOutDTO.originDiscountFee?: 0 else parkOutDTO.discountfee?: 0
        val dayDiscount = if (type == "PAYMENT") parkOutDTO.originDayDiscountFee?: 0 else parkOutDTO.dayDiscountfee?: 0
        val totalDiscount = discount.plus(dayDiscount)

        // 결제 금액 DB 먼저 적제
        parkOutDTO.inSn?.let { inSn ->
            // 1. 할인권 적용 완료 처리
            discountService.applyInoutDiscount(inSn)
            // 결제 금액 저장
            // 사전 정산시 결제금액이 0원이어도 저장, 정상 출차 시에는 skip
            // 기존 정산 데이터가 있을 때 update 진행
            val existSn = inoutPaymentService.findByInSnAndResult(inSn, ResultType.WAIT)?.let { list ->
                if (list.isEmpty())
                    null
                else {
                    // 정산 대기 상태가 있을 경우 결제금액 0인 경우 대기중인 내역 -> del 처리
                    if (payFee == 0) {
                        list.forEach { inoutPaymentDTO ->
                            inoutPaymentDTO.delYn = YN.Y
                            inoutPaymentService.save(inoutPaymentDTO)
                        }
                    }
                    list.sortedByDescending { it.createDate }[0].sn
                }
            }?: kotlin.run { null }

            var inoutPayment = InoutPaymentDTO(
                                    sn = existSn,
                                    inSn = inSn,
                                    vehicleNo = vehicleNo,
                                    type = type,
                                    parkTime = if (type == "PAYMENT") parkOutDTO.originParkTime else parkOutDTO.parktime,
                                    parkFee = if (type == "PAYMENT") parkOutDTO.originParkFee else parkOutDTO.parkfee,
                                    discount = discount,
                                    dayDiscount = dayDiscount,
                                    amount = payFee,
                                    approveDateTime = DateUtil.stringToNowDateTime(),
                                    outSn = parkOutDTO.sn )
            var paymentSn = 0L

            if ( ((type == "PAYMENT" || type == "MANPAYMENT") && payFee > 0) || (type == "PREPAYMENT")) {
                inoutPayment = inoutPaymentService.save(inoutPayment)
                paymentSn = inoutPayment.sn ?: -1L
            }

            displayMessage(parkCarType, payFee.toString() + "원" as String, "WAIT", gate.gateId)

            // 정산기
            // 정산기 보내기전 connetion check
            facilityService.getStatusByGateAndCategory(gate.gateId, FacilityCategoryType.PAYSTATION)?.let { state ->
                if (state["status"] == "NORMAL") {
                    relayClient.sendPayStation(
                        gateId = gate.gateId,
                        type = "adjustmentRequest",
                        requestId = paymentSn.toString(),  //사전 정산시에는 parkIn.SN 으로 정의 -> 모든 정산기 requestId는 정산 SN으로 변경 2021.09.27
                        reqPayStationData(
                            paymentMachineType = if (parkCarType == "NORMAL") "exit" else if (payFee > 0) "exit" else "SEASON",
                            vehicleNumber = vehicleNo,
                            facilitiesId = gate.udpGateid ?: kotlin.run { gate.gateId },
                            recognitionType = if (parkCarType == "NORMAL") "FREE" else if (payFee > 0) "FREE" else "SEASON",
                            recognitionResult = "RECOGNITION",
                            paymentAmount = (inoutPayment.parkFee?: 0).toString(),
                            parktime = parkOutDTO.parktime.toString(),
                            parkTicketMoney = totalDiscount.toString(),  // 할인요금
                            vehicleIntime = DateUtil.formatDateTime(inDate, "yyyy-MM-dd HH:mm"),
                            extendTicket = ticketInfo
                        ),
                        dtFacilityId = dtFacilityId
                    )
                } else {
                    logger.warn { "${gate.gateId} 정산기 접속 상태 [$state[\"status\"]] 오류 처리 $vehicleNo " }
                    // 정산기 접속 오류로 인한 출차 진행
                    inoutPayment.result = ResultType.ERROR
                    inoutPayment.failureMessage = "PAYSTATION tcp connection error"
                    inoutPayment = inoutPaymentService.save(inoutPayment)

                    if (payFee > 0) {
                        if (gate.gateType != GateTypeStatus.ETC) {
                            logger.warn { "${gate.gateId} 정산기 접속 오류 처리 $vehicleNo $payFee 강제 출차" }
                            parkInService.findOne(parkOutDTO.inSn ?: -1)?.let {
                                outFacilityIF(
                                    "ERROR",
                                    parkOutDTO.vehicleNo ?: "",
                                    gate,
                                    parkInMapper.toEntity(it),
                                    parkOutDTO.sn!!
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    fun searchNumberFacilityIF(parkCarType: String, vehicleNo: String, gate: Gate, recognitionResult: String, requestId: String) {
        logger.info { "차량번호 검색 ${gate.gateId} $parkCarType $vehicleNo $recognitionResult, $requestId" }
        relayClient.sendPayStation(
            gateId = gate.gateId,
            type = "adjustmentRequest",
            requestId = requestId,
            reqPayStationData(
                paymentMachineType = "EXIT",
                vehicleNumber = vehicleNo,
                facilitiesId = gate.udpGateid!!,
                recognitionType = "NOTRECOGNITION",
                recognitionResult = recognitionResult,
                paymentAmount = "0",
                vehicleIntime = DateUtil.nowDateTimeHm
            )
        )
        displayMessage("CALL", vehicleNo, "WAIT", gate.gateId)
    }

    fun confirmParkCarType(vehicleNo: String, date: LocalDateTime, type: String): HashMap<String, Any?> {
        var result = HashMap<String, Any?>()
//        var parkCarType = "NORMAL"

        if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
            //todo 정기권 차량 여부 확인
            ticketService.getValidTicket(vehicleNo, date, date)?.let {
                result =
                    hashMapOf<String, Any?>(
                        "parkCarType" to it.ticketType!!.code,
                        "ticketSn" to it.sn
                    )

            }?: kotlin.run {
                result =
                    hashMapOf<String, Any?>(
                        "parkCarType" to "NORMAL"
                    )
            }
        } else {
            result =
                hashMapOf<String, Any?>(
                    "parkCarType" to "UNRECOGNIZED"
                )
        }
        return result
    }
}
