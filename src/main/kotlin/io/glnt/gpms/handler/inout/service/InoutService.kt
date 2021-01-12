package io.glnt.gpms.handler.inout.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqPayData
import io.glnt.gpms.handler.facility.model.reqPayStationData
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
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.SetupOption
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import io.glnt.gpms.model.repository.VehicleListSearchRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

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
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkInRepository: ParkInRepository

    @Autowired
    private lateinit var parkOutRepository: ParkOutRepository

    @Autowired
    private lateinit var vehicleListSearchRepository: VehicleListSearchRepository


    fun parkIn(request: reqAddParkIn) : CommonResult = with(request){
        logger.debug("parkIn service {}", request)
        try {
            // todo gate up(option check)
            val gate = parkinglotService.getGateInfoByFacilityId(facilitiesId)

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

            requestId = parkinglotService.generateRequestId()

            //todo UUID 확인 후 Update
            // 입차 정보 DB insert
            val newData = ParkIn(
                sn = null,
//                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateInfo.gateId,
                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateId,
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
                inDate = inDate,
                uuid = uuid,
                udpssid = if (gate!!.takeAction == "PCC") "11111" else "00000"
            )
            parkInRepository.save(newData)
            parkInRepository.flush()

            // todo 시설 I/F
            // PCC 가 아닌경우애만 아래 모듈 실행
            // 1. gate open

            // 2. 전광판
            // 전광판 메세지 구성은 아래와 같이 진행한다.
            // 'pcc' 인 경우 MEMBER -> MEMBER 로 아닌 경우 MEMBER -> NONMEMBER 로 정의

            if (gate.takeAction != "PCC") {
                // open gate
                facilityService.openGate(gate.gateId, "GATE")
                // 전광판 메세지 출력, gate open
                displayMessage(parkingtype!!, vehicleNo, "IN", gate.gateId)

//                val displayMessage = when (parkingtype) {
//                    "일반차량" -> makeParkPhrase("NONMEMBER", vehicleNo, vehicleNo, "IN")
//                    "미인식차량" -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, "IN")
//                    "정기차량" -> {
//                        val days = productService.calcRemainDayProduct(vehicleNo)
//                        if (days in 1..7)
//                            makeParkPhrase("VIP", vehicleNo, "잔여 0${days}일", "IN")
//                        else
//                            makeParkPhrase("VIP", vehicleNo, vehicleNo, "IN")
//                    }
//                    else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, "IN")
//                }
                /*
                if (tmapSend.equals("ON")) {
                    //todo tmap 전송
                    val data = reqTmapInVehicle(
                        gateId = facility!!.udpGateid!!,
                        inVehicleType = facility.lprType.toString().toLowerCase(),
                        vehicleNumber = vehicleNo,
                        recognitionType = facility.category,
                        recognitorResult = recognitionResult!!,
                        fileUploadId = fileUploadId!!
                    )
                    tmapSendService.sendInVehicle(data, requestId!!, fileName)
                }
            } else {
                if (tmapSend.equals("on")) {
                    //todo tmap 전송
                    val data = reqTmapInVehicle(
                        gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                        inVehicleType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.lprType.toString()
                            .toLowerCase(),
                        vehicleNumber = vehicleNo,
                        recognitionType = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.category,
                        recognitorResult = recognitionResult!!,
                        fileUploadId = fileUploadId!!
                    )
                    tmapSendService.sendInVehicleRequest(data, requestId!!, fileName)
                }
               */
            }

            if (tmapSend.equals("ON")) {
                //todo tmap 전송
                val facility = parkFacilityRepository.findByFacilitiesId(facilitiesId)
                val data = reqTmapInVehicle(
                    gateId = facility!!.udpGateid!!,
                    inVehicleType = facility.lprType.toString().toLowerCase(),
                    vehicleNumber = vehicleNo,
                    recognitionType = facility.category,
                    recognitorResult = recognitionResult!!,
                    fileUploadId = fileUploadId!!
                )
                tmapSendService.sendInVehicle(data, requestId!!, fileName)
            }

            CommonResult.created()

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

    fun searchParkInByVehicleNo(vehicleNo: String) : CommonResult {
        logger.info("VehicleService searchParkInByVehicleNo search param : $vehicleNo")
        parkInRepository.findByVehicleNoEndsWithAndOutSn(vehicleNo, 0L)?.let {
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

    @Transactional(readOnly = true)
    fun parkOut(request: reqAddParkOut) : CommonResult = with(request){
        logger.debug("parkOut service {}", request)
        try {
            // todo uuid 확인 후 skip
            parkOutRepository.findByUuid(uuid)?.let {
                logger.error{ "park out uuid $uuid exists "}
                return CommonResult.exist(request, "park out uuid exists")
            }
            val gate = parkinglotService.getGateInfoByFacilityId(facilitiesId)

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
                productService.getValidProductByVehicleNo(vehicleNo)?.let {
                    parkingtype = "정기차량"
                    validDate = it.validDate
                }
                recognitionResult = "RECOGNITION"

            } else {
                parkingtype = "미인식차량"
                recognitionResult = "NOTRECOGNITION"
            }

            // 전광판 메세지 출력, gate open
            displayMessage(parkingtype!!, vehicleNo, "OUT", gate!!.gateId)
            if (parkingtype == "정기차량")
                facilityService.openGate(gate.gateId, "GATE")

//            requestId = DataCheckUtil.generateRequestId(parkinglotService.parkSiteId())
            requestId = parkinglotService.generateRequestId()

            // 출차 정보 DB insert
            val newData = ParkOut(
                sn = null,
//                gateId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.gateInfo.gateId,
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
                outDate = outDate,
                uuid = uuid
            )
            parkOutRepository.save(newData)
            parkOutRepository.flush()

            // tmap 연동
            if (tmapSend.equals("on")) {
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

            //todo 정산기 출차 전송
            when (parkingtype) {
                "미인식차량" -> {
                    facilityService.sendPaystation(
                        reqPayStationData(paymentMachineType = "EXIT",
                            vehicleNumber = vehicleNo,
                            facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                            recognitionType = "NOTRECOGNITION",
                            recognitionResult = recognitionResult!!,
                            paymentAmount = "0",
                            vehicleIntime = DateUtil.nowDateTimeHm ),
                        gate = gate.gateId,
                        requestId = requestId!!,
                        type = "adjustmentRequest")
                }
                "정기차량" -> {
                    facilityService.sendPaystation(
                        reqPayStationData(paymentMachineType = "SEASON",
                            vehicleNumber = vehicleNo,
                            facilitiesId = parkFacilityRepository.findByFacilitiesId(facilitiesId)!!.udpGateid!!,
                            recognitionType = "SEASON",
                            recognitionResult = recognitionResult!!,
                            paymentAmount = "0",
                            parktime = "0",
                            vehicleIntime = DateUtil.nowDateTimeHm ),
                        gate = gate.gateId,
                        requestId = requestId!!,
                        type = "adjustmentRequest")
                }
            }

            // park-in update
            parkInRepository.findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo, 0L, "N", outDate)?.let { it ->
                it.outSn = newData.sn
                parkInRepository.save(it)
                parkInRepository.flush()
            }

            CommonResult.created()
        } catch (e: RuntimeException) {
            logger.error { "parkout add failed ${e.message}" }
            return CommonResult.error("parkout add failed ")
        }
    }

    @Transactional(readOnly = true)
    fun getAllParkLists(request: reqSearchParkin): ArrayList<ResParkInList>? {
        val pageable: Pageable = PageRequest.of(request.page!!, request.pageSize!!.toInt())
        val results = ArrayList<ResParkInList>()
        when (request.type) {
            DisplayMessageClass.IN -> {
                parkInRepository.findAll(findAllParkInSpecification(request))?.let { list ->
                    list.forEach {
                        val result = ResParkInList(
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
                parkOutRepository.findAll(findAllParkOutSpecification(request), pageable)

            }
            else -> return null
        }
        return results
    }

    private fun findAllParkInSpecification(request: reqSearchParkin): Specification<ParkIn> {
        val spec = Specification<ParkIn> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.vehicleNo != null) {
                val likeValue = "%" + request.vehicleNo + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                )
            }
            if (request.fromDate != null && request.toDate != null) {
                clues.add(
                    criteriaBuilder.between(
                        root.get("inDate"),
                        DateUtil.beginTimeToLocalDateTime(request.fromDate.toString()),
                        DateUtil.beginTimeToLocalDateTime(request.toDate.toString())
                    )
                )
            }

            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }

    private fun findAllParkOutSpecification(request: reqSearchParkin): Specification<ParkOut> {

        val spec = Specification<ParkOut> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.vehicleNo != null) {
                val likeValue = "%" + request.vehicleNo + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                )
            }
            if (request.fromDate != null && request.toDate != null) {
                clues.add(
                    criteriaBuilder.between(
                        root.get("outDate"),
                        DateUtil.beginTimeToLocalDateTime(request.fromDate.toString()),
                        DateUtil.beginTimeToLocalDateTime(request.toDate.toString())
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
                                request.adjustmentAmount.toString()+"원", "OUT", it.gateId!!)
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
                                request.adjustmentAmount.toString()+"원", "OUT", it.gateId!!)

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
                else
                    makeParkPhrase("VIP", vehicleNo, vehicleNo, type)
            }
            "MEMBER" -> makeParkPhrase("MEMBER", vehicleNo, vehicleNo, type)
            else -> makeParkPhrase("FAILNUMBER", vehicleNo, vehicleNo, type)
        }
        facilityService.sendDisplayMessage(displayMessage, gateId)
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