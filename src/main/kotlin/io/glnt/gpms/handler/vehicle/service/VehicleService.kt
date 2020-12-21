package io.glnt.gpms.handler.vehicle.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.tmap.model.reqTmapInVehicle
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.handler.vehicle.model.reqAddParkIn
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File
import java.lang.RuntimeException
import java.time.LocalDate

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
                flag = 1,
                validate = validDate,
                resultcode = resultcode.toInt(),
                requestid = requestId,
                fileuploadid = fileUploadId,
                hour = DateUtil.nowTimeDetail.substring(0,2),
                min = DateUtil.nowTimeDetail.substring(3,5),
                inDate = inDate,
                uuid = uuid
            )
            parkInRepository.save(newData)

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

            // todo 시설 I/F
            // PCC 가 아닌경우애만 아래 모듈 실행
            // 1. gate open

            // 2. 전광판
            // 전광판 메세지 구성은 아래와 같이 진행한다.
            // 'pcc' 인 경우 MEMBER -> MEMBER 로 아닌 경우 MEMBER -> NONMEMBER 로 정의
            if (gate!!.takeAction != "PCC") {
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
                facilityService.sendDisplayMessage(displayMessage)

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
        return CommonResult.notfound("$vehicleNo park in data not found")
    }
}