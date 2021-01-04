package io.glnt.gpms.handler.vehicle.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.tmap.model.reqInOutVehicleInformationSetup
import io.glnt.gpms.handler.tmap.model.reqSendResultResponse
import io.glnt.gpms.handler.tmap.model.reqTmapInVehicle
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.handler.vehicle.model.reqAddParkIn
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.SetupOption
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.lang.RuntimeException
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class VehicleService {
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
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    @Autowired
    private lateinit var parkOutRepository: ParkOutRepository

    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        logger.debug("parkIn service {}", request)
        try {
            // todo gate up(option check)
            val gate = parkinglotService.getGateInfoByFacilityId(facilitiesId)

            // image 파일 저장
            if (base64Str != null) {
                // folder check
                fileFullPath = "$imagePath/"+ LocalDate.now()
                File(fileFullPath!!).apply {
                    if (!exists()) {
                        mkdirs()
                    }
                }
                fileName = parkinglotService.parkSiteId()+"_"+parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid+"_"+ DateUtil.nowTimeDetail.substring(9,12)+vehicleNo+".jpg"
                fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
                val imageByteArray = Base64Util.decodeAsBytes(base64Str!!)
                if (imageByteArray != null) {
                    File("$fileFullPath/$fileName").writeBytes(imageByteArray)
                }
            }

            //차량번호 패턴 체크
            if (DataCheckUtil.isValidCarNumber(vehicleNo)) {
                //todo 출차 정보 확인 후 update

                parkingtype = "일반차량"
                //todo 정기권 차량 여부 확인
                productService.getValidProductByVehicleNo(vehicleNo)?.let {
                    parkingtype = "정기차량"
                    validDate = it.validDate
                }
                recognitionResult = "RECOGNITION"

                // todo 기 입차 여부 확인 및 update
                val parkins = searchParkInByVehicleNo(vehicleNo)
                if (parkins.code == ResultCode.SUCCESS) {
                    val lists = parkins.data as? List<*>?
                    lists!!.checkItemsAre<ParkIn>()?.forEach {
                        it.outSn = -1
                        parkInRepository.save(it)
                    }
                }
            } else {
                parkingtype = "미인식차량"
                recognitionResult = "NOTRECOGNITION"
            }

            requestId = DataCheckUtil.generateRequestId(parkinglotService.parkSiteId())

            //todo 입차 정보 DB insert
            val newData = ParkIn(
                sn = null,
//                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateInfo.gateId,
                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateId,
                parkcartype = parkingtype,
                userSn = 0,
                vehicleNo = vehicleNo,
                image = "$fileFullPath/$fileName",
                flag = 0,
                validate = validDate,
                resultcode = resultcode.toInt(),
                requestid = requestId,
                fileuploadid = fileUploadId,
                hour = DateUtil.nowTimeDetail.substring(0,2),
                min = DateUtil.nowTimeDetail.substring(3,5),
                inDate = inDate,
                uuid = uuid,
                udpssid = if (gate!!.takeAction == "PCC") "11111" else "00000"
            )
            parkInRepository.save(newData)

            // todo 시설 I/F
            // PCC 가 아닌경우애만 아래 모듈 실행
            // 1. gate open

            // 2. 전광판
            // 전광판 메세지 구성은 아래와 같이 진행한다.
            // 'pcc' 인 경우 MEMBER -> MEMBER 로 아닌 경우 MEMBER -> NONMEMBER 로 정의
            if (gate.takeAction != "PCC") {
                // open gate
                facilityService.openGate(gate.gateId, "GATE")
                val displayMessage = when (parkingtype) {
                    "일반차량" -> makeParkInPhrase("NONMEMBER", vehicleNo, vehicleNo)
                    "미인식차량" -> makeParkInPhrase("FAILNUMBER", vehicleNo, vehicleNo)
                    "정기차량" -> {
                        val days = productService.calcRemainDayProduct(vehicleNo)
                        if (days in 1..7)
                            makeParkInPhrase("VIP", vehicleNo, "잔여 0${days}일")
                        else
                            makeParkInPhrase("VIP", vehicleNo, vehicleNo)
                    }
                    else -> makeParkInPhrase("FAILNUMBER", vehicleNo, vehicleNo)
                }
                if (tmapSend.equals("on")) {
                    //todo tmap 전송
                    val data = reqTmapInVehicle(
                        gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                        inVehicleType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.lprType.toString().toLowerCase(),
                        vehicleNumber = vehicleNo,
                        recognitionType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.category,
                        recognitorResult = recognitionResult!!,
                        fileUploadId = fileUploadId!!
                    )
                    tmapSendService.sendInVehicle(data, requestId!!, fileName)
                }
                facilityService.sendDisplayMessage(displayMessage, gate.gateId)
            } else {
                if (tmapSend.equals("on")) {
                    //todo tmap 전송
                    val data = reqTmapInVehicle(
                        gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                        inVehicleType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.lprType.toString().toLowerCase(),
                        vehicleNumber = vehicleNo,
                        recognitionType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.category,
                        recognitorResult = recognitionResult!!,
                        fileUploadId = fileUploadId!!
                    )
                    tmapSendService.sendInVehicleRequest(data, requestId!!, fileName)
                }
            }
            CommonResult.created()

        } catch (e: RuntimeException) {
            ParkinglotService.logger.error("addParkinglotFeature error {} ", e.message)
            return CommonResult.error("parkinglot feature db add failed ")
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

    fun makeParkInPhrase(parkingtype: String, vehicleNo: String, text: String? = null): ArrayList<reqDisplayMessage> {
        val messages = ArrayList<reqDisplayMessage>()
        val lists = when(parkingtype) {
            /* 정기차량 */
            "VIP" -> filterDisplayMessage("IN", DisplayMessageType.VIP)
            /* 티맵회원 */
            "MEMBER" -> filterDisplayMessage("IN", DisplayMessageType.MEMBER)
            /* 일반차량 */
            "NONMEMBER" -> filterDisplayMessage("IN", DisplayMessageType.NONMEMBER)
            /* 번호인식실패 */
            "FAILNUMBER" -> filterDisplayMessage("IN", DisplayMessageType.FAILNUMBER)
            else -> filterDisplayMessage("IN", DisplayMessageType.FAILNUMBER)
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

    fun searchParkInByVehicleNo(vehicleNo: String) : CommonResult {
        logger.info("VehicleService searchParkInByVehicleNo search param : $vehicleNo")
        parkInRepository.findByVehicleNoEndsWithAndOutSn(vehicleNo, 0)?.let {
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
                            parkInRepository.save(ParkIn(
                                sn = null, parkcartype = "일반차량",
                                vehicleNo = request.vehicleNumber,
                                gateId = "GATE001",
                                requestid = requestId,
                                outSn = -100,
                                udpssid = request.sessionId,
                                inDate = DateUtil.stringToLocalDateTime(request.inVehicleDateTime)))
                        }
                    } else {
                        // out
                        if (inVehicle != null) {
                            val outDate = parkOutRepository.findBySn(inVehicle.outSn!!)?.let { outVehicle ->
                                outVehicle.parkcartype = "일반차량"
                                outVehicle.vehicleNo = request.vehicleNumber
                                outVehicle.requestid = requestId
                                outVehicle.outDate = LocalDateTime.now()
                                outVehicle.parktime = DateUtil.diffMins(DateUtil.stringToLocalDateTime(request.inVehicleDateTime), LocalDateTime.now())
                                parkOutRepository.save(outVehicle)
                            } ?: run {
                                parkOutRepository.save(ParkOut(
                                    sn = null, parkcartype = "일반차량",
                                    vehicleNo = request.vehicleNumber,
                                    gateId = "GATE001",
                                    requestid = requestId, outVehicle = 1,
                                    outDate = LocalDateTime.now(),
                                    parktime = DateUtil.diffMins(DateUtil.stringToLocalDateTime(request.inVehicleDateTime), LocalDateTime.now())
                                ))
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
                    tmapSendService.sendInOutVehicleInformationSetupResponse(reqSendResultResponse(result = "FAIL"), DataCheckUtil.generateRequestId(parkinglotService.parkSiteId()))
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "modifyInOutVehicleByTmap failed ${e.message}" }
            tmapSendService.sendInOutVehicleInformationSetupResponse(reqSendResultResponse(result = "FAIL"), DataCheckUtil.generateRequestId(parkinglotService.parkSiteId()))
        }
        tmapSendService.sendInOutVehicleInformationSetupResponse(reqSendResultResponse(result = "SUCCESS"), DataCheckUtil.generateRequestId(parkinglotService.parkSiteId()))
    }

    fun parkOut() {

    }

}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> List<*>.checkItemsAre() =
    if (all { it is T })
        this as List<T>
    else null