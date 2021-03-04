package io.glnt.gpms.handler.facility.service

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.*
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.model.*
import io.glnt.gpms.handler.inout.model.reqUpdatePayment
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.io.glnt.gpms.common.utils.JacksonUtil
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
class FacilityService {
    companion object : KLogging()

    lateinit var gates: List<Gate>

    /* static */
    lateinit var displayColors: List<DisplayColor>

    lateinit var displayMessagesIn: List<DisplayMessage>
    lateinit var displayMessagesOut: List<DisplayMessage>
    lateinit var displayMessagesWait: List<DisplayMessage>

    @Value("\${gateway.url}")
    lateinit var url: String

    @Value("\${tmap.send}")
    lateinit var tmapSend: String

    @Autowired
    private lateinit var displayColorRepository: DisplayColorRepository

    @Autowired
    private lateinit var displayMessageRepository: DisplayMessageRepository

    @Autowired
    private lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var inoutService: InoutService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var relayService: RelayService

    @Autowired
    private lateinit var restAPIManager: RestAPIManagerUtil

    @Autowired
    private lateinit var parkGateRepository: ParkGateRepository

    @Autowired
    private lateinit var facilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var failureRepository: FailureRepository

    @PostConstruct
    fun initalizeData() {
        parkGateRepository.findAll().let {
            gates = it
        }

        val defaultDisplayColor = ArrayList<DisplayColor>()
        defaultDisplayColor.add(DisplayColor(colorCode = "C1", colorDesc = "초록색", sn = null))
        defaultDisplayColor.add(DisplayColor(colorCode = "C3", colorDesc = "하늘색", sn = null))
        defaultDisplayColor.add(DisplayColor(colorCode = "C4", colorDesc = "빨강색", sn = null))
        defaultDisplayColor.add(DisplayColor(colorCode = "C5", colorDesc = "핑크색", sn = null))
        defaultDisplayColor.forEach { displayColor ->
            displayColorRepository.findByColorCode(displayColor.colorCode)?:run {
                displayColorRepository.save(displayColor)
            }
        }

        // 입차/출차 reset 메세지 구성
        val defaultDisplayMessages = ArrayList<DisplayMessage>()
        defaultDisplayMessages.add(
            DisplayMessage(
                messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "안녕하세요", sn = null)
        )
        defaultDisplayMessages.add(
            DisplayMessage(
                messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "환영합니다", sn = null)
        )
        defaultDisplayMessages.add(
            DisplayMessage(
                messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "감사합니다", sn = null)
        )
        defaultDisplayMessages.add(
            DisplayMessage(
                messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "안녕히가세요", sn = null)
        )

        defaultDisplayMessages.forEach { message ->
            displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(message.messageClass!!, message.messageType, message.order!!)?:run {
                displayMessageRepository.save(message)
            }
        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.IN)?.let { meessages ->
            displayMessagesIn = meessages

        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.OUT)?.let { meessages ->
            displayMessagesOut = meessages
        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.WAIT)?.let { meessages ->
            displayMessagesWait = meessages
        }
    }

    // @PostConstruct
    fun fetchDisplayColor() {
        displayMessageRepository.findByMessageClass(DisplayMessageClass.IN)?.let { meessages ->
            displayMessagesIn = meessages

        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.OUT)?.let { meessages ->
            displayMessagesOut = meessages
        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.WAIT)?.let { meessages ->
            displayMessagesWait = meessages
        }
    }

    fun displayOutGate(facilityId: String, line1: String, line2: String) {
//        val data = reqDisplayMessage(
//            line1 = DisplayLine(color = displayColorRepository.findByPositionAndType(DisplayPosition.OUT, DisplayType.NORMAL1)!!.colorCode,
//                                text = line1),
//            line2 = DisplayLine(color = displayColorRepository.findByPositionAndType(DisplayPosition.OUT, DisplayType.NORMAL2)!!.colorCode,
//                                text = line2)
//        )
    }

    fun setDisplayColor(request: ArrayList<reqSetDisplayColor>): CommonResult = with(request) {
        logger.info { "setDisplayColor request $request" }
        try {
            request.forEach { it ->
                displayColorRepository.findByColorCode(it.colorCode)?.let { displayColor ->
                    displayColor.colorCode = it.colorCode
                    displayColor.colorDesc = it.colorDesc
                    displayColorRepository.save(displayColor)
                } ?: run {
                    displayColorRepository.save(
                        DisplayColor(
                            sn = null, colorCode = it.colorCode, colorDesc = it.colorDesc
                        )
                    )
                }
            }
            return CommonResult.created("parkinglot display setting success")

        } catch (e: RuntimeException) {
            logger.error("set display color error {} ", e.message)
            return CommonResult.error("parkinglot display setting failed ")
        }
    }

    fun setDisplayMessage(request: ArrayList<reqSetDisplayMessage>): CommonResult = with(request) {
        logger.info { "setDisplayMessage request $request" }
        try {
            request.forEach { it ->
                displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(
                    it.messageClass!!,
                    it.messageType!!,
                    it.order
                )?.let { displayMessage ->
                    displayMessage.colorCode = it.colorCode
                    displayMessage.messageDesc = it.messageDesc
                    displayMessage.lineNumber = it.line
                    displayMessageRepository.save(displayMessage)
                } ?: run {
                    displayMessageRepository.save(
                        DisplayMessage(
                            sn = null,
                            messageClass = it.messageClass!!,
                            messageType = it.messageType!!,
                            colorCode = it.colorCode,
                            order = it.order,
                            messageDesc = it.messageDesc,
                            lineNumber = it.line
                        )
                    )
                }
            }
            // static upload
            initalizeData()
            return CommonResult.data("display message setting success")
        } catch (e: RuntimeException) {
            logger.error("set display color error {} ", e.message)
            return CommonResult.error("parkinglot display setting failed ")
        }
    }

    fun getDisplayColor() : CommonResult {
        logger.info { "getDisplayColor" }
        try {
            displayColorRepository.findAll().let { it ->
                return CommonResult.data(it)
            }
        }catch (e: RuntimeException){
            logger.error { "getDisplayColor error ${e.message}" }
            return CommonResult.error("getDisplayColor error")
        }
    }
    fun getDisplayMessage() : CommonResult {
        logger.info { "getDisplayMessage" }
        try {
            displayMessageRepository.findAll().let { it ->
                return CommonResult.data(it)
            }
        }catch (e: RuntimeException){
            logger.error { "getDisplayColor error ${e.message}" }
            return CommonResult.error("getDisplayColor error")
        }
    }


    private fun getRelaySvrUrl(gateId: String): String {
        return gates.filter { it.gateId == gateId }[0].relaySvr!!
    }

    fun getGateByGateId(gateId: String) : Gate? {
        return gates.filter { it.gateId == gateId }[0]
    }

//    fun sendDisplayMessage(data: Any, gate: String) {
//        logger.info { "sendPaystation request $data $gate" }
//        parkinglotService.getFacilityByGateAndCategory(gate, "DISPLAY")?.let { its ->
//            its.forEach {
//                restAPIManager.sendPostRequest(
//                    getRelaySvrUrl(gate)+"/display/show",
//                    reqSendDisplay(it.facilitiesId!!, data as ArrayList<reqDisplayMessage>)
//                )
//            }
//        }
//    }

    fun sendPaystation(data: Any, gate: String, requestId: String, type: String) {
        logger.info { "sendPaystation request $data $gate $requestId $type" }
        //todo 정산기 api 연계 개발
        parkinglotService.getFacilityByGateAndCategory(gate, "PAYSTATION")?.let { its ->
            its.forEach {
                restAPIManager.sendPostRequest(
                    getRelaySvrUrl(gate)+"/parkinglot/paystation",
                    reqPaystation(dtFacilityId = it.dtFacilitiesId, data = setPaystationRequest(type, requestId, data))
                )
            }
        }

    }

    @Throws(CustomException::class)
    fun sendPayment(request: reqApiTmapCommon, facilitiesId: String): CommonResult? {
        logger.info { "sendPayment request $request" }
        try{
            var fileName: String? = null
            var fileUploadId: String? = null
//            val contents = JSONUtil.getJSONObject(request.contents.toString()) as reqPayStationData
            val data = JSONUtil.getJsObject(request.contents)
            val contents = readValue(data.toString(), reqPayStationData::class.java) as reqPayStationData
            // park_out update
            inoutService.updatePayment(
                reqUpdatePayment(
                    parkTicketAmount = contents.parkTicketAmount!!.toInt(),
                    paymentAmount = contents.cardAmount!!.toInt(),
                    sn = request.requestId!!.toLong(),
                    cardtransactionId = contents.transactionId!!,
                    cardNumber = contents.cardNumber,
                    approveDateTime = contents.approveDatetime
                )
            )?.let { it ->
                // gate open
                relayService.actionGate(it.gateId!!, "GATE", "open")
                // todo tmap-payment
                tmapSendService.sendTmapInterface(
                    reqSendPayment(
                        vehicleNumber = it.vehicleNo!!,
                        chargingId = it.chargingId!!,
                        paymentMachineType = "EXIT",
                        transactionId = it.cardtransactionid!!,
                        paymentType = "CARD",
                        paymentAmount = it.payfee!!
                    ),
                    parkinglotService.generateRequestId(),
                    "payment"
                )
                fileName = it.image!!
                fileUploadId = it.fileuploadid
            }
            val requestId = parkinglotService.generateRequestId()
            // todo tmap-outvehicle
            tmapSendService.sendOutVehicle(
                reqOutVehicle(
                    gateId = parkinglotService.getGateInfoByFacilityId(facilitiesId)!!.udpGateid!!,
                    seasonTicketYn = "N",
                    vehicleNumber = contents.vehicleNumber,
                    recognitionType = "LPR",
                    recognitorResult = "RECOGNITION",
                    fileUploadId = fileUploadId!!
                ),
                requestId,
                fileName = fileName
            )


        }catch (e: RuntimeException) {
            logger.error("sendPayment {} ", e.message)
            return CommonResult.error("sendPayment failed ")
        }
        return null
    }

    fun setPaystationRequest(type: String, requestId: String?, contents: Any) : reqApiTmapCommon {
        return reqApiTmapCommon(
            type = type,
            parkingSiteId = parkinglotService.parkSiteId()!!,
            requestId = requestId?.let { requestId },
            eventDateTime = DateUtil.stringToNowDateTime(),
            contents = contents
        )
    }

    fun createFacility(facility: Facility): CommonResult {
        logger.info{"createFacility request : $facility"}
        try {
            return CommonResult.data(facilityRepository.save(facility))
        } catch (e: RuntimeException) {
            logger.error { "createFacility error $e" }
            return CommonResult.error("createFacility failed ")
        }
    }

    fun allUpdateFacilities(request: reqUpdateFacilities): CommonResult {
        logger.info("allUpdateFacilities request : $request")
        try {
            request.facilities.forEach {
                facilityRepository.save(it)
            }
            return CommonResult.data(facilityRepository.findAll())
        } catch (e: RuntimeException) {
            logger.error { "allUpdateFacilities error ${e.message}" }
            return CommonResult.error("allUpdateFacilities failed ")
        }
    }

    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
        val factory = JsonFactory()
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        return jacksonObjectMapper().readValue(any, valueType)
    }

    /* udp gate id */
    fun getUdpGateId(gateId: String) : String? {
        return parkGateRepository.findByGateId(gateId)?.udpGateid
    }

    fun updateFacility(facility: Facility): Facility {
        return facilityRepository.save(facility)
    }

    fun updateHealthCheck(dtFacilitiesId: String, status: String) {
        logger.info { "updateHealthCheck facility $dtFacilitiesId status $status" }
        try {
            facilityRepository.findByDtFacilitiesId(dtFacilitiesId)?.let { facility ->
                facility.health = status
                facility.healthDate = LocalDateTime.now()
                facilityRepository.save(facility)
            }
        }catch (e: RuntimeException) {
            logger.error { "allUpdateFacilities error ${e.message}" }
        }
    }

    fun updateStatusCheck(dtFacilitiesId: String, status: String) : Facility? {
        logger.info { "updateStatusCheck facility $dtFacilitiesId status $status" }
        try {
            facilityRepository.findByDtFacilitiesId(dtFacilitiesId)?.let { facility ->
//                if (facility.category == "BREAKER") {
                    facility.status = status
                    facility.statusDate = LocalDateTime.now()
                    return facilityRepository.save(facility)
//                }
            }
        }catch (e: RuntimeException) {
            logger.error { "allUpdateFacilities error ${e.message}" }
        }
        return null
    }

    fun getStatusByGateAndCategory(gateId: String, category: String): HashMap<String, Any?>? {
        try {
            var result = HashMap<String, Any?>()
            facilityRepository.findByGateIdAndCategory(gateId, category)?.let { facilities ->
                val normal = facilities.filter {
                    it.health == "NORMAL"
                }
                when (normal.size) {
                    facilities.size -> {
                        result = hashMapOf(
                            "category" to category,
                            "status" to "NORMAL")
                    }
                    0 -> {
                        result = hashMapOf(
                            "category" to category,
                            "status" to "NORESPONSE")
                    }
                    else -> {
                        result = hashMapOf(
                            "category" to category,
                            "status" to "PARTNORMAL")
                    }
                }
            }
            return result
        }catch (e: RuntimeException) {
            logger.error { "allUpdateFacilities error ${e.message}" }
        }
        return null
    }

    fun getActionByGateAndCategory(gateId: String, category: String): HashMap<String, Any?>? {
        try {
            var result = HashMap<String, Any?>()
            facilityRepository.findByGateIdAndCategory(gateId, category)?.let { facilities ->
                facilities.forEach { facility ->
                    // 장애 상태 확인
                    failureRepository.findTopByFacilitiesIdAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(facility.facilitiesId!!)?.let {
                        return hashMapOf(
                            "category" to category,
                            "status" to facility.status,
                            "failure" to it.failureCode)
                    }
                    result = hashMapOf(
                        "category" to category,
                        "status" to facility.status,
                        "failure" to null)
                }
            }
            return result
        }catch (e: RuntimeException) {
            logger.error { "allUpdateFacilities error ${e.message}" }
        }
        return null
    }

    fun getStatusByGate(gateId: String): HashMap<String, Any?>? {
        try {
            //LPR
            val lpr = getStatusByGateAndCategory(gateId, "LPR")

            //BREAKER
            val breaker = getStatusByGateAndCategory(gateId, "BREAKER")
            val breakerAction = getActionByGateAndCategory(gateId, "BREAKER")

            //DISPLAY
            val display = getStatusByGateAndCategory(gateId, "DISPLAY")

            //PAYSTATION
            val paystation = getStatusByGateAndCategory(gateId, "PAYSTATION")
            val paystationAction = getActionByGateAndCategory(gateId, "PAYSTATION")

            logger.debug { "breaker status ${breaker!!.get("status")} action ${breakerAction!!.get("status")}" }

            return hashMapOf<String, Any?>(
                "lprStatus" to lpr!!["status"],
                "breakerStatus" to breaker!!["status"],
                "breakerAction" to breakerAction!!["status"],
                "breakerFailure" to breakerAction["failure"],
                "displayStatus" to display!!["status"],
                "paystationStatus" to paystation!!["status"],
                "paystationAction" to paystationAction!!["status"],
                "paystationFailure" to paystationAction["failure"]
            )

        }catch (e: CustomException){
            logger.error { "getStatusByGate error ${e.message}" }
        }
        return null
    }
}