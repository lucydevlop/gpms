package io.glnt.gpms.handler.inout.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqPayData
import io.glnt.gpms.handler.facility.model.reqPayStationData
import io.glnt.gpms.handler.facility.model.reqPaymentResult
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.*
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.entity.VehicleListSearch
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import io.glnt.gpms.model.repository.VehicleListSearchRepository
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
import kotlin.concurrent.timer

@Service
class InoutService {
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
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    @Autowired
    private lateinit var parkOutRepository: ParkOutRepository

    @Autowired
    private lateinit var vehicleListSearchRepository: VehicleListSearchRepository


    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        logger.info{"parkIn service car_num:${request.vehicleNo} facility_id:${request.facilitiesId} in_date:${request.date} result_code:${request.resultcode} uuid:${request.uuid}"}
        try {
            // UUID 없을 경우(Back 입차) deviceIF -> OFF 로 전환
            if (uuid == null) deviceIF = "OFF"

            // todo gate up(option check)
            parkinglotService.getGateInfoByFacilityId(facilitiesId) ?.let { gate ->
                // image 파일 저장
                if (base64Str != null) {
                    fileFullPath = saveImage(base64Str!!, vehicleNo, facilitiesId)
//                fileName = fileFullPath!!.substring(fileFullPath!!.lastIndexOf("/")+1)
                    fileName = DataCheckUtil.getFileName(fileFullPath!!)
                    fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                }

                //차량번호 패턴 체크
                if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                    parkingtype = "일반차량"
                    // 정기권 차량 여부 확인
                    productService.getValidProductByVehicleNo(vehicleNo)?.let {
                        parkingtype = "정기차량"
                        validDate = it.validDate
                    }
                    recognitionResult = "RECOGNITION"

                    // 기 입차 여부 확인 및 update
                    val parkins = searchParkInByVehicleNo(vehicleNo, gate.gateId)
                    if (parkins.code == ResultCode.SUCCESS.getCode()) {
                        val lists = parkins.data as? List<*>?
                        lists!!.checkItemsAre<ParkIn>()?.forEach {
                            it.outSn = -1
                            parkInRepository.save(it)
                            parkInRepository.flush()
                        }
                    }
                } else {
                    parkingtype = "미인식차량"
                    recognitionResult = "NOTRECOGNITION"
                }

                requestId = parkinglotService.generateRequestId()

                // UUID 확인 후 Update
                parkInRepository.findByUuid(uuid!!)?.let {
                    deviceIF = "OFF"
                    inSn = it.sn
                    requestId = it.requestid
                }

                // Back 입차 시
                if (uuid == null) {
                    parkInRepository.findByVehicleNoEndsWithAndOutSnAndGateId(vehicleNo, 0, gate.gateId)?.let {
                        return CommonResult.data(it)
                    }
                }

                // 시설 I/F
                // PCC 가 아닌경우애만 아래 모듈 실행
                // 1. gate open

                // 2. 전광판
                // 전광판 메세지 구성은 아래와 같이 진행한다.
                // 'pcc' 인 경우 MEMBER -> MEMBER 로 아닌 경우 MEMBER -> NONMEMBER 로 정의

                if (gate.takeAction != "PCC" && deviceIF == "ON") {
                    // todo GATE 옵션인 경우 정기권/WHITE OPEN 옵션 정의
                    if (gate.openAction == "SEASONTICKET" && parkingtype != "정기차량") {
                        displayMessage("RESTRICTE", vehicleNo, "IN", gate.gateId)
                        return CommonResult.data("Restricte vehicle $vehicleNo $parkingtype")

                    }
                    // open gate
                    facilityService.openGate(gate.gateId, "GATE")
                    // 전광판 메세지 출력, gate open
                    displayMessage(parkingtype!!, vehicleNo, "IN", gate.gateId)
                }

                // 입차 정보 DB insert
                val newData = ParkIn(
                    sn = request.inSn?.let { request.inSn } ?: run { null },
//                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateInfo.gateId,
                    gateId = gate.gateId,
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
                    inDate = date,
                    uuid = uuid,
                    udpssid = if (gate.takeAction == "PCC") "11111" else "00000"
                )
                parkInRepository.save(newData)
                parkInRepository.flush()

                if (parkinglotService.parkSite.tmapSend.equals("ON")) {
                    //todo tmap 전송
                    val facility = parkFacilityRepository.findByFacilitiesId(facilitiesId)
                    val data = reqTmapInVehicle(
                        gateId = facilityService.getUdpGateId(facility!!.gateId)!!,
                        inVehicleType = facility.lprType.toString().toLowerCase(),
                        vehicleNumber = vehicleNo,
                        recognitionType = facility.category,
                        recognitionResult = recognitionResult!!,
                        fileUploadId = fileUploadId!!
                    )
                    tmapSendService.sendInVehicle(data, requestId!!, fileName)
                }
                return CommonResult.data(newData)
            }
            logger.error("parkIn error failed gateId is not found {} ", facilitiesId )
            return CommonResult.error("parkin failed gateId is not found ")

        } catch (e: RuntimeException) {
            logger.error("parkIn error {} ", e.message)
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
                        inVehicle.delYn = "Y"
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

    fun saveImage(base64Str: String, vehicleNo: String, facilitiesId: String) : String {
        val fileFullPath: String = "$imagePath/"+ LocalDate.now()
        File(fileFullPath).apply {
            if (!exists()) {
                mkdirs()
            }
        }
        val fileName = parkinglotService.parkSiteId()+"_"+parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid+"_"+ DateUtil.nowTimeDetail.substring(
            9,
            12
        )+vehicleNo+".jpg"
        val imageByteArray = Base64Util.decodeAsBytes(base64Str)
        if (imageByteArray != null) {
            File("$fileFullPath/$fileName").writeBytes(imageByteArray)
        }
        return "$fileFullPath/$fileName"
    }

//    @Transactional(readOnly = true)
    fun parkOut(request: reqAddParkOut) : CommonResult = with(request){
        logger.info{"parkOut service car_number: ${request.vehicleNo} out_date: ${request.date} facilityId: ${request.facilitiesId}"}
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

            parkinglotService.getGateInfoByFacilityId(facilitiesId)?.let { gate ->

                // image 파일 저장
                if (base64Str != null) {
                    fileFullPath = saveImage(base64Str!!, vehicleNo, facilitiesId)
                    fileName = fileFullPath!!.substring(fileFullPath!!.lastIndexOf("/")+1)

                    fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                }

                //차량번호 패턴 체크
                if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                    parkingtype = "일반차량"
                    //todo 정기권 차량 여부 확인
                    productService.getValidProductByVehicleNo(vehicleNo, date, date)?.let {
                        parkingtype = "정기차량"
                        validDate = it.validDate
                    }
                    recognitionResult = "RECOGNITION"

                    if (!parkOutRepository.findByVehicleNoEndsWith(vehicleNo).isNullOrEmpty()) {
                        return CommonResult.exist(request.vehicleNo, "park out vehicleNo exists")
                    }

                    // park-in update
                    parkInRepository.findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo, 0L, "N", date)?.let { it ->
                        parkIn = it
                    }

                } else {
                    parkingtype = "미인식차량"
                    recognitionResult = "NOTRECOGNITION"
                }

                // gate 옵션인 경우 요금계산 진행
//                if (gate.takeAction == "GATE") {
                if (parkIn != null)  {
                    price = feeCalculation.getBasicPayment(parkIn!!.inDate!!, date, VehicleType.SMALL, vehicleNo, 1, 0)
                    logger.info { "-------------------getBasicPayment Result -------------------" }
                    logger.info { "입차시간 : $parkIn!!.inDate!! / 출차시간 : $date / 주차시간: ${price!!.parkTime}" }
                    logger.info { "총 요금 : ${price!!.orgTotalPrice} / 결제 요금 : ${price!!.totalPrice}" }
                }
//                }

                // 출차 정보 DB insert
                val newData = ParkOut(
                    sn = request.outSn?.let { request.outSn }?.run { null },
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
                    parktime = price?.let { price!!.parkTime }?.run { null },
                    parkfee = price?.let { price!!.orgTotalPrice }?.run { null },
                    payfee = price?.let { price!!.totalPrice }?.run { null }
                )
                parkOutRepository.save(newData)
                parkOutRepository.flush()

                // tmap 연동
                if (parkinglotService.parkSite.tmapSend.equals("ON")) {
                    when (parkingtype) {
                        "정기차량" -> tmapSendService.sendOutVehicle(
                            reqOutVehicle(
                                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                                seasonTicketYn = "Y",
                                vehicleNumber = vehicleNo,
                                recognitionType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.category,
                                recognitorResult = recognitionResult!!,
                                fileUploadId = fileUploadId!! ),
                            requestId!!, fileName)
                        "일반차량" -> {
                            tmapSendService.sendAdjustmentRequest(
                                reqAdjustmentRequest(
                                    gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                                    paymentMachineType = "exit",
                                    vehicleNumber = vehicleNo,
                                    recognitionType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.category,
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
                    //todo 정산기 출차 전송
                    when (parkingtype) {
                        "미인식차량" -> {
                            facilityService.sendPaystation(
                                reqPayStationData(
                                    paymentMachineType = "EXIT",
                                    vehicleNumber = vehicleNo,
                                    facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                                    recognitionType = "NOTRECOGNITION",
                                    recognitionResult = recognitionResult!!,
                                    paymentAmount = "0",
                                    vehicleIntime = DateUtil.nowDateTimeHm
                                ),
                                gate = gate.gateId,
                                requestId = requestId!!,
                                type = "adjustmentRequest"
                            )
                            // displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
                        }
                        "정기차량" -> {
                            if (price!!.totalPrice == 0) {
                                facilityService.sendPaystation(
                                    reqPayStationData(
                                        paymentMachineType = "SEASON",
                                        vehicleNumber = vehicleNo,
                                        facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                                        recognitionType = "SEASON",
                                        recognitionResult = "RECOGNITION",
                                        paymentAmount = price!!.totalPrice.toString(),
                                        parktime = price!!.parkTime.toString(),
                                        vehicleIntime = DateUtil.nowDateTimeHm
                                    ),
                                    gate = gate.gateId,
                                    requestId = newData.sn.toString(),
                                    type = "adjustmentRequest"
                                )
                                displayMessage(parkingtype!!, vehicleNo, "OUT", gate.gateId)
                                facilityService.openGate(gate.gateId, "GATE")
                            } else {
                                facilityService.sendPaystation(
                                    reqPayStationData(
                                        paymentMachineType = "SEASON",
                                        vehicleNumber = vehicleNo,
                                        facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                                        recognitionType = "SEASON",
                                        recognitionResult = recognitionResult!!,
                                        paymentAmount = "0",
                                        parktime = "0",
                                        vehicleIntime = DateUtil.nowDateTimeHm
                                    ),
                                    gate = gate.gateId,
                                    requestId = requestId!!,
                                    type = "adjustmentRequest"
                                )
                            }
                        }
                        "일반차량" -> {
                            if (gate.takeAction != "PCC") {
                                facilityService.sendPaystation(
                                    reqPayStationData(
                                        paymentMachineType = "exit",
                                        vehicleNumber = vehicleNo,
                                        facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                                        recognitionType = "FREE",
                                        recognitionResult = "RECOGNITION",
                                        paymentAmount = price!!.totalPrice.toString(),
                                        parktime = price!!.parkTime.toString(),
                                        vehicleIntime = DateUtil.formatDateTime(parkIn!!.inDate!!),
                                        adjustmentDateTime = DateUtil.nowDateTime
                                    ),
                                    gate = gate.gateId,
                                    requestId = newData.sn.toString(),
                                    type = "adjustmentRequest"
                                )

                                if (price!!.discountPrice!! > 0) {
                                    Thread.sleep(200)

                                    facilityService.sendPaystation(
                                        reqPayData(
                                            paymentMachineType = "exit",
                                            vehicleNumber = vehicleNo,
                                            parkTicketType = "OK",
                                            parkTicketMoney = price!!.discountPrice.toString(),  // 할인요금
                                            facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!
                                        ),
                                        gate = gate.gateId,
                                        requestId = newData.sn.toString(),
                                        type = "adjustmentdataRequest"
                                    )
                                }
                            }
                        }
                    }
                }

                parkInRepository.findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo, 0L, "N", date)?.let { parkIn ->
                    parkIn.outSn = newData.sn
                    parkInRepository.save(parkIn)
                    parkInRepository.flush()
                }

                return CommonResult.created()
            }
            return CommonResult.error("parkout add failed ")
        } catch (e: RuntimeException) {
            logger.error { "parkout add failed ${e.stackTrace}" }
            return CommonResult.error("parkout add failed ")
        }
    }

    @Transactional(readOnly = true)
    fun getAllParkLists(request: reqSearchParkin): CommonResult {
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
                                inGateId = it.gateId, inDate = it.inDate!!
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
                            results.add(result)
                        }
                    }
                }
                DisplayMessageClass.OUT -> {
//                parkOutRepository.findAll(findAllParkOutSpecification(request))

                }
                else -> return CommonResult.error("getAllParkLists failed")
            }
            return CommonResult.data(results)
        } catch (e: RuntimeException) {
            logger.error { "getAllParkLists ${e.message}" }
            return CommonResult.error("getAllParkLists failed")
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
                            facilityService.openGate(it.gateId!!, "GATE")

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
                        facilityService.openGate(it.gateId!!, "GATE")
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
        val displayMessage = when (parkingtype) {
            "일반차량" -> makeParkPhrase("NONMEMBER", vehicleNo, vehicleNo, type)
            "미인식차량" -> {
                when (type) {
                    "IN" -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
                    "OUT" -> makeParkPhrase("CALL", vehicleNo, vehicleNo, type)
                    else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
                }
            }
            "정기차량" -> {
                val days = productService.calcRemainDayProduct(vehicleNo)
                if (days in 1..7)
                    makeParkPhrase("VIP", vehicleNo, "잔여 0${days}일", type)
                else {

                    makeParkPhrase("VIP", vehicleNo, vehicleNo, type)
                }
            }
            "MEMBER" -> makeParkPhrase("MEMBER", vehicleNo, vehicleNo, type)
            "RESTRICTE" -> makeParkPhrase("RESTRICTE", vehicleNo, vehicleNo, type)
            else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
        }
        facilityService.sendDisplayMessage(displayMessage, gateId)
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
                    inGateId = it.gateId, inDate = it.inDate!!, inImgBase64Str = Base64Util.encodeAsString(File(it.image!!).readBytes())
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

    @Transactional(readOnly = true)
    fun updateInout(request: resParkInList) : CommonResult {
        logger.info { "updateInout request $request" }
        try {
            var inResult: Any? = null
            parkinglotService.getFacilityByGateAndCategory(request.inGateId!!, "LPR")?.let { its ->
                its.filter { it.lprType == LprTypeStatus.INFRONT }
            }?.let { facilies ->
                val result = parkIn(
                    reqAddParkIn(vehicleNo = request.vehicleNo!!,
                        facilitiesId = facilies[0].dtFacilitiesId,
                        date = request.inDate,
                        resultcode = "0",
                        base64Str = request.inImgBase64Str,
                        uuid = UUID.randomUUID().toString(),
                        inSn = if (request.parkinSn == 0L) null else request.parkinSn,
                        deviceIF = "OFF"
                    )).data!! as ParkIn
                inResult = parkInRepository.findBySn(result.sn!!)!!
            }?.run {
                return CommonResult.error("updateInout failed")
            }
            request.outDate?.let {
                parkinglotService.getFacilityByGateAndCategory(request.inGateId!!, "LPR")?.let { its ->
                    its.filter { it.lprType == LprTypeStatus.OUTFRONT }
                }?.let { facilies ->
                    var newOut = parkOut(
                        reqAddParkOut(vehicleNo = request.vehicleNo!!,
                            facilitiesId = facilies[0].dtFacilitiesId,
                            date = request.outDate!!,
                            base64Str = request.outImgBase64Str,
                            uuid = UUID.randomUUID().toString(),
                            resultcode = "0",
                            outSn = request.parkoutSn,
                            deviceIF = "OFF"
                        ))
                }?.run {
                    return CommonResult.error("updateInout failed")
                }
            }
            return CommonResult.data()
        }catch (e: RuntimeException){
            logger.error { "updateInout failed ${e.message}" }
            return CommonResult.error("updateInout failed")
        }
    }

    fun paymentResult(request: reqPaymentResult, requestId: String, gateId: String) : CommonResult {
        logger.info { "paymentResult $request" }
        try {
            parkOutRepository.findBySn(requestId.toLong())?.let { it ->
                it.cardtransactionid = request.transactionId
                it.approveDatetime = request.approveDatetime
                it.cardNumber = request.cardNumber
                parkOutRepository.save(it)

                facilityService.openGate(gateId, "GATE")
                displayMessage(
                    it.parkcartype!!,
                    request.vehicleNumber, "OUT", gateId)

                if (parkinglotService.parkSite.tmapSend.equals("ON")) {
                    //todo tmap 전송
                    val data = reqSendPayment(
                        vehicleNumber = request.vehicleNumber,
                        chargingId = it.chargingId!!,
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

    fun makeParkOutPhrase(parkingtype: String, vehicleNo: String, text: String? = null) {
        val messages = ArrayList<reqDisplayMessage>()
//        when(parkingtype) {
//            "일반차량" -> {
//                val lists = filterDisplayMessage("IN", DisplayMessageType.NONMEMBER)
//                lists.forEach { list ->
//                    val message = reqDisplayMessage(
//                        order = list.order, line = list.lineNumber,
//                        color = list.displayColor!!.colorCode,
//                        text = if (list.messageDesc == "-") text!! else list.messageDesc,
//                    )
//                    messages.add(message)
//                }
//            }
//            "정기차량" -> {
//                val diff = productService.calcRemainDayProduct(vehicleNo)
//                if (diff >=0 && diff < 8) {
//                    // 만기도래 별도 처리
//                    var message = reqDisplayMessage(
//                        order = 1, line = 1,
//                        text = "정기권차량", color = "C1"
//                    )
//                    messages.add(message)
//                    message = reqDisplayMessage(
//                        order = 1, line = 1,
//                        text = "정기권차량", color = "C1"
//                    )
//
//
//                }
////                else {
////                    val lists = facilityService.displayMessages.filter { it.messageType == DisplayMessageType.VIP }
////                        .sortedBy { it.order }
////                    lists.forEach { list ->
////                        val message = reqDisplayMessage(
////                           order = list.order, line = list.lineNumber,
////                           text = list.messageDesc, color = list.displayColor!!.colorCode
////                        )
////                        messages.add(message)
////                    }
////                }
//            }
//        }
    }

}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.checkItemsAre() =
    if (all { it is T })
        this as List<T>
    else null