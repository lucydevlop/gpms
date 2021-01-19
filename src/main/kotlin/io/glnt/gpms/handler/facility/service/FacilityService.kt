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
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.io.glnt.gpms.common.utils.JacksonUtil
import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.entity.VehicleListSearch
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

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
    private lateinit var restAPIManager: RestAPIManagerUtil

    @Autowired
    private lateinit var vehicleListSearchRepository: VehicleListSearchRepository

    @Autowired
    private lateinit var parkGateRepository: ParkGateRepository

    @Autowired
    private lateinit var facilityRepository: ParkFacilityRepository

    fun fetchGate() {
        parkGateRepository.findAll().let {
            gates = it
        }
    }

    fun openGate(id: String, type: String) {
        logger.trace { "openGate request $type $id" }
        try {
            when (type) {
                "GATE" -> {
                    parkinglotService.getFacilityByGateAndCategory(id, "BREAKER")?.let { its ->
                        its.forEach {
                            val url = getRelaySvrUrl(id)
                            restAPIManager.sendGetRequest(
                                url+"/breaker/${it.facilitiesId}/open"
                            )
                        }
                    }
                }
                else -> {
                    val url = getRelaySvrUrl(parkinglotService.getFacility(id)!!.gateId)
                    restAPIManager.sendGetRequest(
                        url+"/breaker/${id}/open"
                    )
                }
            }
        } catch (e: RuntimeException) {
            logger.error {  "openGate ${type} ${id} error ${e.message}"}
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
        logger.trace { "setDisplayColor request $request" }
        try {
            request.forEach { it ->
                displayColorRepository.findByPositionAndType(it.position!!, it.type!!)?.let { displayColor ->
                    displayColor.colorCode = it.colorCode
                    displayColor.colorDesc = it.colorDesc
                    displayColorRepository.save(displayColor)
                } ?: run {
                    displayColorRepository.save(
                        DisplayColor(
                            sn = null,
                            position = it.position!!, type = it.type!!,
                            colorCode = it.colorCode, colorDesc = it.colorDesc
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
        logger.trace { "setDisplayMessage request $request" }
        try {
            request.forEach { it ->
                displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(
                    it.messageClass!!,
                    it.messageType!!,
                    it.order
                )?.let { displayMessage ->
                    displayMessage.colorType = it.colorType
                    displayMessage.messageDesc = it.messageDesc
                    displayMessage.lineNumber = it.line
                    displayMessageRepository.save(displayMessage)
                } ?: run {
                    displayMessageRepository.save(
                        DisplayMessage(
                            sn = null,
                            messageClass = it.messageClass!!,
                            messageType = it.messageType!!,
                            colorType = it.colorType,
                            order = it.order,
                            messageDesc = it.messageDesc,
                            lineNumber = it.line
                        )
                    )
                }
            }
            // static upload
            fetchDisplayColor()

            return CommonResult.created("display message setting success")

        } catch (e: RuntimeException) {
            logger.error("set display color error {} ", e.message)
            return CommonResult.error("parkinglot display setting failed ")
        }
    }

    fun fetchDisplayColor() {
        val positions: List<DisplayPosition> = listOf(DisplayPosition.IN, DisplayPosition.OUT)
        displayColorRepository.findByPositionIn(positions).let {
            displayColors = it
        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.IN)?.let { meessages ->
            displayMessagesIn = meessages

            displayMessagesIn.forEach { it ->
                displayColorRepository.findByPositionAndType(DisplayPosition.IN, it.colorType!!)?.let { color ->
                    it.displayColor = color
                }
            }
        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.OUT)?.let { meessages ->
            displayMessagesOut = meessages

            displayMessagesOut.forEach { it ->
                displayColorRepository.findByPositionAndType(DisplayPosition.OUT, it.colorType!!)?.let { color ->
                    it.displayColor = color
                }
            }
        }

        displayMessageRepository.findByMessageClass(DisplayMessageClass.WAIT)?.let { meessages ->
            displayMessagesWait = meessages

            displayMessagesWait.forEach { it ->
                displayColorRepository.findByPositionAndType(DisplayPosition.OUT, it.colorType!!)?.let { color ->
                    it.displayColor = color
                }
            }
        }
    }
    private fun getRelaySvrUrl(gateId: String): String {
        return gates.filter { it.gateId == gateId }[0].relaySvr!!
    }

    fun sendDisplayMessage(data: Any, gate: String) {
        logger.trace { "sendPaystation request $data $gate" }
        parkinglotService.getFacilityByGateAndCategory(gate, "DISPLAY")?.let { its ->
            its.forEach {
                restAPIManager.sendPostRequest(
                    getRelaySvrUrl(gate)+"/display/show",
                    reqSendDisplay(it.facilitiesId!!, data as ArrayList<reqDisplayMessage>)
                )
            }
        }
    }

    fun sendPaystation(data: Any, gate: String, requestId: String, type: String) {
        logger.trace { "sendPaystation request $data $gate $requestId $type" }
        //todo 정산기 api 연계 개발
        parkinglotService.getFacilityByGateAndCategory(gate, "PAYSTATION")?.let { its ->
            its.forEach {
                restAPIManager.sendPostRequest(
                    getRelaySvrUrl(gate)+"/parkinglot/paystation",
                    reqPaystation(facilityId = it.facilitiesId!!, data = setPaystationRequest(type, requestId, data))
                )
            }
        }

    }

    @Throws(CustomException::class)
    fun searchCarNumber(request: reqSendVehicleListSearch): CommonResult? {
        logger.trace { "searchCarNumber request $request" }
        if (parkinglotService.parkSite.tmapSend == "ON") {
            // table db insert
            val requestId = parkinglotService.generateRequestId()
            vehicleListSearchRepository.save(VehicleListSearch(requestId = requestId, facilityId = request.facilityId))
            tmapSendService.sendTmapInterface(request, requestId, "vehicleListSearch")
        } else {
            return inoutService.searchParkInByVehicleNo(request.vehicleNumber)
        }
        return null
    }

    @Throws(CustomException::class)
    fun sendPayment(request: reqApiTmapCommon, facilitiesId: String): CommonResult? {
        logger.trace { "sendPayment request $request" }
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
                openGate(it.gateId!!, "GATE")
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
}