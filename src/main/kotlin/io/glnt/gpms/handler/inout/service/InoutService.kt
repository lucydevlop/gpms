package io.glnt.gpms.handler.inout.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqPayData
import io.glnt.gpms.handler.facility.model.reqPayStationData
import io.glnt.gpms.handler.facility.model.reqPaymentResult
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.*
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import javax.persistence.criteria.Predicate
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

@Service
class InoutService(
    private var inoutPaymentRepository: InoutPaymentRepository
) {
    companion object : KLogging()

    @Value("\${image.filepath}")
    lateinit var imagePath: String

    @Value("\${tmap.send}")
    lateinit var tmapSend: String

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var tmapSendService: TmapSendService

    @Autowired
    lateinit var facilityService: FacilityService

    @Autowired
    lateinit var feeCalculation: FeeCalculation

    @Autowired
    lateinit var relayService: RelayService

    @Autowired
    lateinit var discountService: DiscountService

    @Autowired
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    @Autowired
    private lateinit var parkOutRepository: ParkOutRepository

    @Autowired
    private lateinit var vehicleListSearchRepository: VehicleListSearchRepository


    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        logger.warn{"parkIn service car_num:${request.vehicleNo} facility_id:${request.dtFacilitiesId} in_date:${request.date} result_code:${request.resultcode} uuid:${request.uuid}"}
        try {

            // gate up(option check)
            // todo 요일제 차량 옵션 적용
            parkinglotService.getGateInfoByDtFacilityId(dtFacilitiesId) ?.let { gate ->
                // UUID 없을 경우(Back 입차) deviceIF -> OFF 로 전환
                // 동일 입차 처리 skip
                if (uuid!!.isEmpty()) {
                    deviceIF = "OFF"
                    if (parkInRepository.findByVehicleNoEndsWithAndOutSnAndGateIdAndDelYn(vehicleNo, 0, gate.gateId, DelYn.N)!!.isNotEmpty()) {
                        logger.warn{" 기 입차 car_num:${request.vehicleNo} skip "}
                        return CommonResult.data()
                    }
                } else {
                    requestId = parkinglotService.generateRequestId()
                    // UUID 확인 후 Update
                    parkInRepository.findByUuid(uuid!!)?.let {
                        if (it.vehicleNo == vehicleNo) {
                            logger.warn{" 기 입차 car_num:${request.vehicleNo} skip "}
                            return CommonResult.data()
                        }
                        deviceIF = "OFF"
                        // inSn = it.sn
                        // requestId = it.requestid
                        if (resultcode == "0" || resultcode.toInt() >= 100) { return CommonResult.data() }

                    }
                }

                val facility = parkinglotService.getFacilityByDtFacilityId(dtFacilitiesId)
                // 만차 제어 설정 시 count 확인 후 skip
                if (parkinglotService.parkSite!!.space != null) {
                    val spaces = parkinglotService.parkSite!!.space as ParkSiteInfo.spaceAttributes
//                    parkinglotService.parkSite.space!!.spaces!!.forEach { it ->
                    spaces.spaces!!.forEach {
                        if (it.gate.contains(gate.gateId) || it.gate.contains("ALL")) {
                            if (parkInRepository.countByGateIdAndOutSn(gate.gateId, 0) >= it.space) {
                                displayMessage("FULL", vehicleNo, "IN", gate.gateId)
                                logger.warn{" car_num:${request.vehicleNo} 만차 skip "}
                                return CommonResult.data("Full limit $vehicleNo $parkingtype")
                            }
                        }
                    }
                }
                // image 파일 저장
                if (base64Str != null) {
                    fileFullPath = saveImage(base64Str!!, vehicleNo, gate.udpGateid!!)
//                fileName = fileFullPath!!.substring(fileFullPath!!.lastIndexOf("/")+1)
                    fileName = DataCheckUtil.getFileName(fileFullPath!!)
                    fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                }

                //차량번호 패턴 체크
                if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                    parkingtype = "NORMAL"
                    // 정기권 차량 여부 확인
                    productService.getValidProductByVehicleNo(vehicleNo, date, date)?.let {
                        parkingtype = it.ticketType!!.code
                        validDate = it.validDate
                        ticketSn = it.sn
                    }
                    recognitionResult = "RECOGNITION"

                    // 기 입차 여부 확인 및 update
                    val parkins = searchParkInByVehicleNo(vehicleNo, gate.gateId)
                    if (parkins.code == ResultCode.SUCCESS.getCode()) {
                        val lists = parkins.data as? List<ParkIn>?
                        lists!!.filter { it.outSn == 0L }.forEach {
                            it.outSn = -1
                            parkInRepository.save(it)
                            parkInRepository.flush()
                        }
                    }
                } else {
                    parkingtype = "UNRECOGNIZED"
                    recognitionResult = "NOTRECOGNITION"
                }

                //차량 요일제 적용
                parkinglotService.parkSite!!.vehicleDayOption?.let {
                    if (recognitionResult == "RECOGNITION" && it != VehicleDayType.OFF) {
                        if (DataCheckUtil.isRotation(it, vehicleNo)) {
                        } else {
                            displayMessage("RESTRICTE", vehicleNo, "IN", gate.gateId)
                            return CommonResult.data("Restricte vehicle $vehicleNo $parkingtype")
                        }
                    }
                }


                // 입차 정보 DB insert
                val newData = ParkIn(
                    sn = request.inSn?.let { request.inSn } ?: run { null },
                    gateId = gate.gateId,
                    parkcartype = parkingtype,
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
                    memo = memo
                )
                parkInRepository.save(newData)
                parkInRepository.flush()

                // 시설 I/F
                // PCC 가 아닌경우애만 아래 모듈 실행
                // 1. gate open

                // 2. 전광판
                // 전광판 메세지 구성은 아래와 같이 진행한다.
                // 'pcc' 인 경우 MEMBER -> MEMBER 로 아닌 경우 MEMBER -> NONMEMBER 로 정의
                if (gate.takeAction != "PCC" && deviceIF == "ON") {
                    // todo GATE 옵션인 경우 정기권/WHITE OPEN 옵션 정의
                    when(gate.openAction){
                        OpenActionType.RECOGNITION -> {
                            if ("UNRECOGNIZED" == parkingtype) {
                                displayMessage("RESTRICTE", vehicleNo, "IN", gate.gateId)
                                return CommonResult.data("Restricte vehicle $vehicleNo $parkingtype")
                            }
                        }
                        OpenActionType.RESTRICT -> {
                            if ("NORMAL" == parkingtype || "UNRECOGNIZED" == parkingtype) {
                                displayMessage("RESTRICTE", vehicleNo, "IN", gate.gateId)
                                return CommonResult.data("Restricte vehicle $vehicleNo $parkingtype")
                            }
                        }
                        else -> {

                        }
                    }
                    displayMessage(parkingtype!!, vehicleNo, "IN", gate.gateId)
                    relayService.actionGate(gate.gateId, "GATE", "open")

                }

                if (parkinglotService.isTmapSend()) {
                    //todo tmap 전송
                    val data = reqTmapInVehicle(
                        gateId = gate.udpGateid!!,
                        inVehicleType = facility!!.lprType.toString().toLowerCase(),
                        vehicleNumber = vehicleNo,
                        recognitionType = facility.category,
                        recognitionResult = recognitionResult!!,
                        fileUploadId = fileUploadId!!
                    )
                    tmapSendService.sendInVehicle(data, requestId!!, fileName)
                }
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
            else -> filterDisplayMessage(type, DisplayMessageType.FAILNUMBER)
        }
        lists.forEach { list ->
            val message = reqDisplayMessage(
                order = list.order!!, line = list.lineNumber!!,
                color = list.displayColor!!.colorCode,
                text = if (list.messageDesc == "-") text!! else list.messageDesc
            )
            messages.add(message)
        }
        return messages
    }

    fun filterDisplayMessage(messageClass: String, type: DisplayMessageType): List<DisplayMessage> {
        return when (messageClass) {
            "IN" -> facilityService.displayMessagesIn.filter { it.messageType == type }
                .sortedBy { it.order }
            "WAIT" -> facilityService.displayMessagesWait.filter { it.messageType == type }
                .sortedBy { it.order }
            else -> facilityService.displayMessagesOut.filter { it.messageType == type }
                .sortedBy { it.order }
        }
    }

    fun searchParkInByVehicleNo(vehicleNo: String, gateId: String) : CommonResult {
        logger.trace("VehicleService searchParkInByVehicleNo search param : $vehicleNo $gateId")

        parkInRepository.findAll(findAllParkInSpecification(reqSearchParkin(searchLabel = "CARNUM", searchText = vehicleNo, gateId = gateId)))?.let { it ->
            if (it.isNullOrEmpty()) return CommonResult.notfound("$vehicleNo park in data not found")
            return CommonResult.data(it)
        }

        return CommonResult.notfound("$vehicleNo park-in data not found")
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
                            val outDate = parkOutRepository.findBySn(inVehicle.outSn!!)?.let { outVehicle ->
                                outVehicle.parkcartype = "일반차량"
                                outVehicle.vehicleNo = request.vehicleNumber
                                outVehicle.requestid = requestId
                                outVehicle.outDate = LocalDateTime.now()
                                outVehicle.parktime = DateUtil.diffMins(
                                    DateUtil.stringToLocalDateTime(request.inVehicleDateTime),
                                    LocalDateTime.now()
                                )
                                parkOutRepository.save(outVehicle)
                            } ?: run {
                                parkOutRepository.save(
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
                            }
                            inVehicle.outSn = outDate.sn
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
                        inVehicle.delYn = DelYn.Y
                        inVehicle.requestid = requestId
                        inVehicle.udpssid = request.sessionId
                        parkInRepository.save(inVehicle)
                    }
                }
                else -> {
                    tmapSendService.sendTmapInterface(
                        reqSendResultResponse(result = "FAIL"), parkinglotService.generateRequestId(),
                        "inOutVehicleInformationSetupResponse"
                    )
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "modifyInOutVehicleByTmap failed ${e.message}" }
            tmapSendService.sendTmapInterface(
                reqSendResultResponse(result = "FAIL"), parkinglotService.generateRequestId(),
                "inOutVehicleInformationSetupResponse"
            )
        }
        tmapSendService.sendTmapInterface(
            reqSendResultResponse(result = "SUCCESS"), parkinglotService.generateRequestId(),
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
        val fileName = parkinglotService.parkSiteId()+"_"+udpGateid+"_"+ DateUtil.nowTimeDetail.substring(
            9,
            12
        )+vehicleNo+".jpg"
        val imageByteArray = Base64Util.decodeAsBytes(base64Str)
        if (imageByteArray != null) {
            File("$fileFullPath/$fileName").writeBytes(imageByteArray)
        }
        return "$fileFullPath/$fileName"
    }

    fun parkOut(request: reqAddParkOut) : CommonResult = with(request){
        logger.warn{"출차 event car_number: ${request.vehicleNo} out_date: ${request.date} facilityId: ${request.dtFacilitiesId} uuid: ${request.uuid}"}
        try {
            if (requestId.isNullOrEmpty()) {
                requestId = parkinglotService.generateRequestId()
            }

            // uuid 확인 후 skip
            parkOutRepository.findByUuid(uuid)?.let {
                logger.info{ "park out uuid $uuid exists $it "}
                if (it.parkcartype != "미인식차량" && DataCheckUtil.isValidCarNumber(vehicleNo)) {
                    logger.error { "park out uuid $uuid exists " }
                    return CommonResult.exist(request.uuid, "park out uuid exists")
                }
                requestId = it.requestid
                parkOut = it
                outSn = it.sn
            }

            parkinglotService.getGateInfoByDtFacilityId(dtFacilitiesId)?.let { gate ->
                val facility = parkinglotService.getFacilityByDtFacilityId(dtFacilitiesId)
                // image 파일 저장
                if (base64Str != null) {
                    fileFullPath = saveImage(base64Str!!, vehicleNo, gate.udpGateid!!)
                    fileName = fileFullPath!!.substring(fileFullPath!!.lastIndexOf("/")+1)

                    fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                }

                //차량번호 패턴 체크
                if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                    parkingtype = "NORMAL"
                    //todo 정기권 차량 여부 확인
                    productService.getValidProductByVehicleNo(vehicleNo, date, date)?.let {
                        parkingtype = it.ticketType!!.code
                        validDate = it.validDate
                    }
                    recognitionResult = "RECOGNITION"

                    // park-in update
                    parkInRepository.findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo, 0L, DelYn.N, date)?.let { it ->
                        logger.warn { "입차 확인 sn car_num ${it.sn} car_num ${it.vehicleNo} " }
                        parkIn = it

                        parkOutRepository.findTopByInSnAndDelYnOrderByOutDateDesc(it.sn!!, DelYn.N)?.let { exist ->
                            requestId = exist.requestid
                            parkOut = exist
                            outSn = exist.sn
                            logger.warn { "미출차 확인 sn car_num ${request.outSn} car_num ${exist.vehicleNo} " }
                        }
                    }

                    if (parkIn == null && !parkOutRepository.findByVehicleNoEndsWith(vehicleNo).isNullOrEmpty()) {
                        logger.error { "출차 데이터 입차 내역에 없음 ${request.vehicleNo} " }
                        //전광판 내역 표기 추가
                        displayMessage("CALL", vehicleNo, "WAIT", gate.gateId)
                        // todo 정산기 번호 검색 추가
                        return CommonResult.exist(request.vehicleNo, "출차 데이터 입차 내역에 없음 ${request.vehicleNo}")
                    }

                } else {
                    parkingtype = "UNRECOGNIZED"
                    recognitionResult = "NOTRECOGNITION"
                }

                displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)

                if (parkinglotService.parkSite!!.saleType == SaleType.PAID && parkIn != null) {
                    price = feeCalculation.getBasicPayment(parkIn!!.inDate!!, date, VehicleType.SMALL, vehicleNo, 1, 0, parkIn!!.sn)
                    logger.warn { "-------------------getBasicPayment Result -------------------" }
                    logger.warn { "입차시간 : $parkIn!!.inDate!! / 출차시간 : $date / 주차시간: ${price!!.parkTime}" }
                    logger.warn { "총 요금 : ${price!!.orgTotalPrice} / 결제 요금 : ${price!!.totalPrice} / 할인 요금 : ${price!!.discountPrice} / 일최대할인요금 : ${price!!.dayilyMaxDiscount}" }
                }

//                logger.warn { "미출차 확인 sn car_num ${request.outSn} car_num ${vehicleNo} " }

                // 출차 정보 DB insert
                val newData = ParkOut(
                    sn = request.outSn?.let { request.outSn }?: kotlin.run { null },
                    gateId = gate.gateId, //parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateId,
                    parkcartype = parkingtype,
                    userSn = 0,
                    vehicleNo = vehicleNo,
                    image = "$fileFullPath",
                    flag = 0,
                    validate = validDate,
                    resultcode = resultcode.toInt(),
                    requestid = requestId,
                    fileuploadid = fileUploadId,
                    hour = DateUtil.nowTimeDetail.substring(0, 2),
                    min = DateUtil.nowTimeDetail.substring(3, 5),
                    outDate = date,
                    uuid = uuid,
                    parktime = if (price == null) parkIn?.let { DateUtil.diffMins(parkIn!!.inDate!!, date) }?: kotlin.run { 0 } else price!!.parkTime,
                    parkfee = if (price == null) null else price!!.orgTotalPrice,
                    payfee = if (price == null) null else price!!.totalPrice,
                    discountfee = if (price == null) null else price!!.discountPrice,
                    dayDiscountfee = if (price == null) null else price!!.dayilyMaxDiscount,
                    inSn = parkIn?.sn ?: kotlin.run { null }
                )
                parkOutRepository.saveAndFlush(newData)

                // tmap 연동
                if (parkinglotService.isTmapSend()) {
                    when (parkingtype) {
                        "SEASONTICKET", "WHITELIST" -> tmapSendService.sendOutVehicle(
                            reqOutVehicle(
                                gateId = gate.udpGateid!!,
                                seasonTicketYn = "Y",
                                vehicleNumber = vehicleNo,
                                recognitionType = facility!!.category,
                                recognitorResult = recognitionResult!!,
                                fileUploadId = fileUploadId!! ),
                            requestId!!, fileName)
                        "NORMAL" -> {
                            tmapSendService.sendAdjustmentRequest(
                                reqAdjustmentRequest(
                                    gateId = gate.udpGateid!!,
                                    paymentMachineType = "exit",
                                    vehicleNumber = vehicleNo,
                                    recognitionType = facility!!.category,
                                    facilitiesId = parkFacilityRepository.findByGateIdAndCategory(gate.gateId, "PAYSTATION")?.get(0)!!.facilitiesId!!,
                                    fileuploadId = fileUploadId!!
                                ),
                                requestId!!
                            )
                            vehicleListSearchRepository.save(VehicleListSearch(requestId, parkFacilityRepository.findByGateIdAndCategory(gate.gateId, "PAYSTATION")?.get(0)!!.facilitiesId!!))
                        }
                    }
                }

                if (deviceIF == "ON") {
                    // 정산기 출차 전송
                    when (parkingtype) {
                        "UNRECOGNIZED" -> {
                            facilityService.sendPaystation(
                                reqPayStationData(
                                    paymentMachineType = "EXIT",
                                    vehicleNumber = vehicleNo,
                                    facilitiesId = gate.udpGateid!!,
                                    recognitionType = "NOTRECOGNITION",
                                    recognitionResult = recognitionResult!!,
                                    paymentAmount = "0",
                                    vehicleIntime = DateUtil.nowDateTimeHm
                                ),
                                gate = gate.gateId,
                                requestId = requestId!!,
                                type = "adjustmentRequest"
                            )
                            displayMessage(parkingtype!!, vehicleNo, "WAIT", gate.gateId)
                        }
                        else -> {
                            displayMessage(parkingtype!!, if (price != null) (price!!.orgTotalPrice!!-price!!.dayilyMaxDiscount!!).toString()+"원" else "0원", "WAIT", gate.gateId)

                            facilityService.sendPaystation(
                                reqPayStationData(
                                    paymentMachineType = if (parkingtype == "NORMAL") "exit" else "SEASON",
                                    vehicleNumber = vehicleNo,
                                    facilitiesId = gate.udpGateid!!,
                                    recognitionType = if (parkingtype == "NORMAL") "FREE" else "SEASON",
                                    recognitionResult = "RECOGNITION",
                                    paymentAmount = if (price != null) (price!!.orgTotalPrice!!-price!!.dayilyMaxDiscount!!).toString() else "0",
                                    parktime = if (price != null) price!!.parkTime.toString() else newData.parktime?.let { newData.parktime.toString()}?.run { "0" },
                                    parkTicketMoney = if (price != null) price!!.discountPrice!!.toString() else "0",  // 할인요금
                                    vehicleIntime = DateUtil.nowDateTimeHm
                                ),
                                gate = gate.gateId,
                                requestId = newData.sn.toString(),
                                type = "adjustmentRequest"
                            )
//                            price?.let {
//                                if (price!!.discountPrice!! > 0) {
//                                    Thread.sleep(200)
//
//                                    facilityService.sendPaystation(
//                                        reqPayData(
//                                            paymentMachineType = "exit",
//                                            vehicleNumber = vehicleNo,
//                                            parkTicketType = "OK",
//                                            parkTicketMoney = price!!.discountPrice.toString(),  // 할인요금
//                                            facilitiesId = gate.udpGateid!!
//                                        ),
//                                        gate = gate.gateId,
//                                        requestId = newData.sn.toString(),
//                                        type = "adjustmentdataRequest"
//                                    )
//                                }
//                            }
                        }
                    }
                    // 동일 입차 출차 처리
                    parkIn?.let { updateParkInExitComplete(it, newData.sn!! ) }
                    // 전광판 display 전송
                    if (parkinglotService.parkSite!!.saleType == SaleType.FREE) {
                        //parkIn?.let { updateParkInExitComplete(it, newData.sn!! ) }
                        //displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
                        logger.warn { "parkout car_number: ${request.vehicleNo} 출차 gate ${gate.gateId} open" }
                        relayService.actionGate(gate.gateId, "GATE", "open")
                    } else {
                        if (gate.openAction == OpenActionType.NONE && (price!!.totalPrice == 0)) {
                            //parkIn?.let { updateParkInExitComplete(it, newData.sn!! ) }
                            //displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
                            logger.warn { "parkout car_number: ${request.vehicleNo} 출차 gate ${gate.gateId} open" }
                            relayService.actionGate(gate.gateId, "GATE", "open")
                        }
                    }
                }

                logger.warn { "parkout car_number: ${request.vehicleNo} 출차 성공" }
                return CommonResult.created()
            }
            logger.error { "parkout car_number: ${request.vehicleNo} 출차 failed" }
            return CommonResult.error("parkout car_number: ${request.vehicleNo} 출차 failed ")
        } catch (e: CustomException) {
            logger.error { "parkout car_number: ${request.vehicleNo} 출차 failed $e" }
            return CommonResult.error("parkout add failed ")
        }
    }

    fun updateParkInExitComplete(data: ParkIn, outSn: Long) : Boolean {
        try {
            data.outSn = outSn
            parkInRepository.saveAndFlush(data)

            // 동일 UUID 에 대해서 del_ny 처리
            data.uuid?.let { inUuid ->
                parkInRepository.findByUuidAndOutSnAndDelYn(inUuid, 0, DelYn.N)?.let { ins ->
                    ins.forEach {
                        it.delYn = DelYn.Y
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

    @Transactional(readOnly = true)
    fun getAllParkLists(request: reqSearchParkin): ArrayList<resParkInList>? {
        logger.info { "getAllParkLists $request" }
        try {
            val results = ArrayList<resParkInList>()
            when (request.searchDateLabel) {
                DisplayMessageClass.IN -> {
                    parkInRepository.findAll(findAllParkInSpecification(request))?.let { list ->
                        list.forEach {
                            val result = resParkInList(
                                type = DisplayMessageClass.IN,
                                parkinSn = it.sn!!, vehicleNo = it.vehicleNo, parkcartype = it.parkcartype!!,
                                inGateId = it.gateId, inDate = it.inDate!!,
                                ticketCorpName = it.ticket?.corp?.corpName, memo = it.memo,
                                inImgBase64Str = it.image?.let { image -> image.substring(image.indexOf("/park")) }
                            )
                            result.paymentAmount = inoutPaymentRepository.findByInSnAndResultAndDelYn(it.sn!!, ResultType.SUCCESS, DelYn.N)?.let { payment ->
                                payment.sumBy { it.amount!! }
                            }?: kotlin.run { 0 }

                            if (it.outSn!! > 0L && it.outSn != null) {
                                parkOutRepository.findBySn(it.outSn!!)?.let { out ->
                                    result.type = DisplayMessageClass.OUT
                                    result.parkoutSn = out.sn
                                    result.outDate = out.outDate
                                    result.outGateId = out.gateId
                                    result.parktime = out.parktime
                                    result.parkfee = out.parkfee
                                    result.payfee = out.payfee
                                    result.discountfee = out.discountfee
                                    result.dayDiscountfee = out.dayDiscountfee
                                    result.outImgBase64Str = if (out.image!!.contains("/park")) out.image!!.substring(out.image!!.indexOf("/park")) else null
                                }
                            } else {
                                result.parkoutSn = it.outSn
                            }
                            results.add(result)
                        }
                    }
                }
                DisplayMessageClass.OUT -> {
                    parkOutRepository.findAll(findAllParkOutSpecification(request))?.let{ list ->
                        list.forEach { out ->
                            parkInRepository.findTopByOutSnAndDelYnOrderByInDateDesc(out.sn!!, DelYn.N)?.let { parkin ->
                                val result = resParkInList(
                                    type = DisplayMessageClass.OUT,
                                    parkinSn = parkin.sn!!, vehicleNo = parkin.vehicleNo, parkcartype = parkin.parkcartype!!,
                                    inGateId = parkin.gateId, inDate = parkin.inDate!!,
                                    ticketCorpName = parkin.ticket?.corp?.corpName,
                                    parkoutSn = out.sn , outDate = out.outDate, outGateId = out.gateId, parktime = out.parktime,
                                    parkfee = out.parkfee, payfee = out.payfee, discountfee = out.discountfee
                                )
                                results.add(result)
                            }
                        }
                    }
                }
                else -> return null
            }
            return results
        } catch (e: RuntimeException) {
            logger.error { "getAllParkLists $e" }
            return null
        }
//        val pageable: Pageable = PageRequest.of(request.page!!, request.pageSize!!.toInt())

    }

    private fun findAllParkInSpecification(request: reqSearchParkin): Specification<ParkIn> {
        val spec = Specification<ParkIn> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.searchLabel == "CARNUM" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                )
            }
            if (request.fromDate != null && request.toDate != null) {
                clues.add(
                    criteriaBuilder.between(
                        root.get("inDate"),
                        DateUtil.beginTimeToLocalDateTime(request.fromDate.toString()),
                        DateUtil.lastTimeToLocalDateTime(request.toDate.toString())
                    )
                )
            }
            if (request.gateId != null) {
                val likeValue = "%" + request.gateId + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("gateId")), likeValue)
                )
            }
            clues.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N))
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("inDate")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }

    private fun findAllParkOutSpecification(request: reqSearchParkin): Specification<ParkOut> {

        val spec = Specification<ParkOut> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.searchLabel == "CARNUM" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                )
            }
            if (request.fromDate != null && request.toDate != null) {
                clues.add(
                    criteriaBuilder.between(
                        root.get("outDate"),
                        DateUtil.beginTimeToLocalDateTime(request.fromDate.toString()),
                        DateUtil.lastTimeToLocalDateTime(request.toDate.toString())
                    )
                )
            }
            clues.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }

    fun adjustmentRequestResponse(request: reqAdjustmentRequestResponse, requestId: String) {
        // 5s skip
        parkOutRepository.findByRequestid(requestId)?.let {  it ->
            if (DateUtil.diffSecs(it.UpdateDate!!, LocalDateTime.now()) <= 5) {
                when(request.result) {
                    "SUCCESS" -> {
                        // open Gate
                        if (request.outVehicleAllowYn == "Y" || request.adjustmentAmount == 0) {
                            relayService.actionGate(it.gateId!!, "GATE", "open")

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
                        relayService.actionGate(it.gateId!!, "GATE", "open")
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
        parkOutRepository.findBySn(request.sn)?.let { it ->
            it.discountfee = parkTicketAmount
            it.payfee = paymentAmount
            it.cardtransactionid = cardtransactionId
//            it.parkcartype = parkcarType
            it.cardNumber = cardNumber!!.substring(4)
            it.outVehicle = 1
            it.approveDatetime = approveDateTime

            displayMessage(it.parkcartype!!, it.vehicleNo!!, "OUT", it.gateId!!)
            return parkOutRepository.save(it)
        }
        return null
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
                val days = productService.calcRemainDayProduct(vehicleNo)
                if (days in 1..7)
                    makeParkPhrase("VIP", vehicleNo, "잔여 0${days}일", type)
                else {

                    makeParkPhrase("VIP", vehicleNo, vehicleNo, type)
                }
            }
            "MEMBER", "RESTRICTE", "FULL", "INIT", "CALL" -> makeParkPhrase(parkingtype, vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("RESTRICTE", vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("FULL", vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("INIT", vehicleNo, vehicleNo, type)
//             -> makeParkPhrase("")
            else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
        }
        relayService.sendDisplayMessage(displayMessage, gateId, reset)
    }

    fun lastSettleData(facilityId: String): ParkOut? {
        return parkOutRepository.findTopByPaystationOrderByOutDateDesc(facilityId)
    }

    @Transactional(readOnly = true)
    fun getParkInOutDetail(request: Long): CommonResult {
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
                    parkOutRepository.findBySn(it.outSn!!)?.let { out ->
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
            var inResult: Any? = null
            parkinglotService.getFacilityByGateAndCategory(request.inGateId!!, "LPR")?.let { its ->
                its.filter { it.lprType == LprTypeStatus.FRONT }
            }?.let { facilies ->
                val result = parkIn(
                    reqAddParkIn(vehicleNo = request.vehicleNo!!,
                        dtFacilitiesId = facilies[0].dtFacilitiesId,
                        date = request.inDate,
                        resultcode = "0",
                        base64Str = request.inImgBase64Str,
                        uuid = UUID.randomUUID().toString(),
                        inSn = if (request.parkinSn == 0L) null else request.parkinSn,
                        deviceIF = "OFF", memo = request.memo
                    )).data!! as ParkIn
                inResult = parkInRepository.findBySn(result.sn!!)!!
            }?.run {
                return CommonResult.error("updateInout failed")
            }
        }catch (e: RuntimeException){
            logger.error { "createInout failed $e" }
            return CommonResult.error("createInout failed")
        }
        return CommonResult.data()
    }

    @Throws(CustomException::class)
    fun updateInout(request: resParkInList) : CommonResult {
        logger.info { "updateInout request $request" }
        try {
            parkInRepository.findBySn(request.parkinSn!!)?.let { parkIn ->
                parkIn.inDate = request.inDate
                request.inGateId?.let { parkIn.gateId = request.inGateId }
                request.vehicleNo?.let { parkIn.vehicleNo = request.vehicleNo }
                parkIn.parkcartype = request.parkcartype
                request.memo?.let{ parkIn.memo = request.memo}

                parkInRepository.save(parkIn)
                parkInRepository.flush()
            }
//            var inResult: Any? = null
//            parkinglotService.getFacilityByGateAndCategory(request.inGateId!!, "LPR")?.let { its ->
//                its.filter { it.lprType == LprTypeStatus.INFRONT }
//            }?.let { facilies ->
//                val result = parkIn(
//                    reqAddParkIn(vehicleNo = request.vehicleNo!!,
//                        dtFacilitiesId = facilies[0].dtFacilitiesId,
//                        date = request.inDate,
//                        resultcode = "0",
//                        base64Str = request.inImgBase64Str,
//                        uuid = UUID.randomUUID().toString(),
//                        inSn = if (request.parkinSn == 0L) null else request.parkinSn,
//                        deviceIF = "OFF"
//                    )).data!! as ParkIn
//                inResult = parkInRepository.findBySn(result.sn!!)!!
//            }?.run {
//                return CommonResult.error("updateInout failed")
//            }
//            request.outDate?.let {
//                parkinglotService.getFacilityByGateAndCategory(request.inGateId!!, "LPR")?.let { its ->
//                    its.filter { it.lprType == LprTypeStatus.OUTFRONT }
//                }?.let { facilies ->
//                    parkOut(
//                        reqAddParkOut(vehicleNo = request.vehicleNo!!,
//                            dtFacilitiesId = facilies[0].dtFacilitiesId,
//                            date = request.outDate!!,
//                            base64Str = request.outImgBase64Str,
//                            uuid = UUID.randomUUID().toString(),
//                            resultcode = "0",
//                            outSn = request.parkoutSn,
//                            deviceIF = "OFF"
//                        ))
//                }?.run {
//                    return CommonResult.error("updateInout failed")
//                }
//            }
            return CommonResult.data()
        }catch (e: RuntimeException){
            logger.error { "updateInout failed ${e.message}" }
            return CommonResult.error("updateInout failed")
        }
    }

    fun deleteInout(sn: Long) : CommonResult {
        try{
            parkInRepository.findBySn(sn)?.let { parkIn ->
                parkIn.delYn = DelYn.Y
                parkInRepository.save(parkIn)
                parkInRepository.flush()
                parkIn.outSn?.let {
                    if (parkIn.outSn!! > 0L){
                        parkOutRepository.findBySn(parkIn.outSn!!)?.let { parkOut ->
                            parkOut.delYn = DelYn.Y
                            parkOutRepository.save(parkOut)
                            parkOutRepository.flush()
                        }
                    }
                }
            }
            return CommonResult.data()
        }catch (e: RuntimeException){
            logger.error { "deleteInout failed $e" }
            return CommonResult.error("deleteInout failed")
        }
    }

    fun paymentResult(request: reqPaymentResult, requestId: String, gateId: String) : CommonResult {
        logger.info { "paymentResult $request" }
        try {
            parkOutRepository.findBySn(requestId.toLong())?.let { out ->
                out.cardtransactionid = request.transactionId
                out.approveDatetime = request.approveDatetime
                out.cardNumber = request.cardNumber
                parkOutRepository.save(out)

                var inSn = out.inSn

                //할인 데이터도 적용 완료 처리
//                parkInRepository.findByOutSnAndDelYn(out.sn!!, DelYn.N)?.let { parkin->
//                    parkin.forEach {
//                        discountService.applyInoutDiscount(it.sn!!)
//                        updateParkInExitComplete(it, out.sn!! )
//                    }
//                    inSn = parkin[parkin.lastIndex].sn
//                }
                discountService.applyInoutDiscount(inSn!!)
                parkInRepository.findBySn(inSn!!)?.let {
                    updateParkInExitComplete(it, out.sn!! )
                }


                //결제 테이블 적재
                inoutPaymentRepository.save(
                    InoutPayment(sn = null, inSn = inSn, outSn = out.sn, approveDateTime = request.approveDatetime,
                        payType = PayType.CARD, amount = request.cardAmount?.toInt() ?: kotlin.run { null }, cardCorp = request.cardCorp, cardNumber = request.cardNumber,
                    transactionId = request.transactionId, result = if (request.failureMessage == null) ResultType.SUCCESS else ResultType.FAILURE, failureMessage = request.failureMessage)
                )

                relayService.actionGate(gateId, "GATE", "open")
                displayMessage(
                    out.parkcartype!!,
                    request.vehicleNumber, "OUT", gateId)

                if (parkinglotService.isTmapSend()) {
                    //todo tmap 전송
                    val data = reqSendPayment(
                        vehicleNumber = request.vehicleNumber,
                        chargingId = out.chargingId!!,
                        paymentMachineType = "EXIT",
                        transactionId = request.transactionId!!,
                        paymentType = "CARD",
                        paymentAmount = request.cardAmount!!
                    )
                    tmapSendService.sendPayment(data, parkinglotService.generateRequestId())
                }
                return CommonResult.data()
            }
            return CommonResult.error("paymentResult parkout is not found")
        }catch (e: RuntimeException){
            logger.error { "paymentResult failed ${e.message}" }
            return CommonResult.error("paymentResult failed")
        }
    }

    fun getImagePath(imagePath: String?): String? {
        return if (imagePath != null && imagePath.contains("/park", true)) { imagePath.substring(imagePath.indexOf("/park")).replace("//", "/") }
            else null
    }

    fun getLastInout(type: GateTypeStatus, gateId: String ): HashMap<String, Any?>? {
        try {
            var result = HashMap<String, Any?>()
            when (type) {
                GateTypeStatus.IN -> {
                    parkInRepository.findTopByGateIdAndDelYnOrderByInDateDesc(gateId, DelYn.N
//                        , DateUtil.minusSecLocalDateTime(
//                        LocalDateTime.now(), 10)
                    )?.let {
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
                    parkOutRepository.findTopByGateIdAndDelYnOrderByOutDateDesc(gateId, DelYn.N
//                        , DateUtil.minusSecLocalDateTime(
//                        LocalDateTime.now(), 10)
                    )?.let {
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
                    parkInRepository.findTopByGateIdAndDelYnOrderByInDateDesc(gateId, DelYn.N
//                        , DateUtil.minusSecLocalDateTime(
//                        LocalDateTime.now(), 10)
                    )?.let { parkIn ->
                        parkOutRepository.findTopByGateIdAndDelYnOrderByOutDateDesc(gateId, DelYn.N
//                        , DateUtil.minusSecLocalDateTime(
//                        LocalDateTime.now(), 10)
                        )?.let { parkOut ->
                            if (parkIn.inDate!! > parkOut.outDate) {
                                result =
                                    hashMapOf<String, Any?>(
                                        "gateId" to gateId,
                                        "vehicleNo" to parkIn.vehicleNo,
                                        "date" to parkIn.inDate,
                                        "carType" to parkIn.parkcartype,
                                        "image" to getImagePath(parkIn.image) )
                            } else {
                                result =
                                    hashMapOf<String, Any?>(
                                        "gateId" to gateId,
                                        "vehicleNo" to parkOut.vehicleNo,
                                        "date" to parkOut.outDate,
                                        "carType" to parkOut.parkcartype,
                                        "image" to getImagePath(parkOut.image) )
                            }
                        }?.run {
                            result =
                                hashMapOf<String, Any?>(
                                    "gateId" to gateId,
                                    "vehicleNo" to parkIn.vehicleNo,
                                    "date" to parkIn.inDate,
                                    "carType" to parkIn.parkcartype,
                                    "image" to getImagePath(parkIn.image) )
                        }
                    }?.run {
                        parkOutRepository.findTopByGateIdAndDelYnOrderByOutDateDesc(gateId, DelYn.N
//                        , DateUtil.minusSecLocalDateTime(
//                        LocalDateTime.now(), 10)
                        )?.let {
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
                }
            }
            return result
        }catch (e: CustomException) {
            logger.error { "getLastInout failed ${e.message}" }
            return null
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.checkItemsAre() =
    if (all { it is T })
        this as List<T>
    else null
