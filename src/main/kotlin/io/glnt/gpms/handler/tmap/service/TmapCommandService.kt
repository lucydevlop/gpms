package io.glnt.gpms.handler.tmap.service

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JSONUtil
import io.glnt.gpms.handler.facility.model.reqParkingSiteInfo
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.service.FacilityService
import io.glnt.gpms.service.InoutService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.model.entity.TmapCommand
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.TmapCommandRepository
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import kotlin.collections.ArrayList


@Service
class TmapCommandService(
    private var parkSiteInfoService: ParkSiteInfoService
) {
    companion object : KLogging()

    @Autowired
    private lateinit var tmapCommandRepository: TmapCommandRepository

    @Autowired
    private lateinit var facilityService: FacilityService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var inoutService: InoutService

    @Autowired
    private lateinit var relayService: RelayService

    fun getRequestCommand(request: reqApiTmapCommon) {
        val response = request //.commandData

        response.contents = JSONUtil.getJsObject(response.contents)
        // db insert
        tmapCommandRepository.save(
            TmapCommand(
                sn = null,
                parkingSiteId = response.parkingSiteId,
                type = response.type,
                responseId = response.responseId?.run { response.requestId },
                eventDateTime = response.commandDateTime?.run { response.eventDateTime },
                contents = response.contents.toString()
            )
        )

        when(response.type) {
            "parkingsiteinfo" -> { commandParkingSiteInfo() }
            "dspcolorinfo" -> { facilityService.fetchDisplayColor() }
            "profileSetup" -> { commandProfileSetup(response) }
            "facilitiesRegistResponse" -> { }
            "facilitiesCommand" -> { commandFacilities(response) }
            "gateTakeActionSetup" -> { commandGateTakeActionSetup(response) }
            "vehicleListSearchResponse" -> { commandVehicleListSearch(response) }
            "inOutVehicleInformationSetup" -> { commandInOutVehicleInformationSetup(response) }
            "adjustmentRequestResponse" -> { commandAdjustmentRequestResponse(response) }
            else -> {}
        }
    }

    fun commandAdjustmentRequestResponse(request: reqApiTmapCommon) {
        val contents : reqAdjustmentRequestResponse = request.contents as reqAdjustmentRequestResponse
        inoutService.adjustmentRequestResponse(contents, request.responseId!!)
    }

    fun commandParkingSiteInfo() {
        val parkSite = parkSiteInfoService.parkSite
        val contents = reqParkingSiteInfo(
            parkingSiteName = parkSite!!.siteName!!,
            lotNumberAddress = "--",
            roadNameAddress = parkSite.address!!,
            detailsAddress = "**",
            telephoneNumber = parkSite.tel!!,
            saupno = parkSite.saupno!!,
            businessName = parkSite.ceoname!!
        )

        parkinglotService.getFacilityByCategory(FacilityCategoryType.PAYSTATION)?.let { its ->
            its.forEach { it ->
                facilityService.sendPaystation(contents, it.gateId, DateUtil.stringToNowDateTime(), "ParkingInfo")
            }
        }
    }

    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
        val data = JSONUtil.getJSONObject(any)
        val factory = JsonFactory()
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        return jacksonObjectMapper().readValue(data.toString(), valueType)
    }

    /* request 'facilitiesCommand' */
    fun commandFacilities(request: reqApiTmapCommon) {
        val contents = readValue(request.contents.toString(), reqCommandFacilities::class.java)

        // todo facilityid check
        contents.BLOCK?.let { it ->
            when(it) {
                "OPEN" -> {
                    // gate
                    relayService.actionGate(contents.facilitiesId, "FACILITY", "open")
                    // display
                    val facility = parkinglotService.getFacility(contents.facilitiesId)
//                    facilityService.displayOutGate(facility!!.gateId, "감사합니다", "안녕히가세요")
                }
                "CLOSE" -> {
                }
                "OPENLOCKING" -> {
                }
                "RELEASE" -> {
                }
                else -> {}
            }
        }
    }

    /* request 'profileSetup' */
    fun commandProfileSetup(request: reqApiTmapCommon) {
        val contents = readValue(request.contents.toString(), reqCommandProfileSetup::class.java)

        val parkSite = parkSiteInfoService.parkSite
        // parksite update
        contents.facilitiesStatusNotiCycle?.let {  parkSite?.facilitiesStatusNotiCycle = contents.facilitiesStatusNotiCycle!!.toInt() }
        contents.parkingSpotStatusnotiCycle?.let {  parkSite?.parkingSpotStatusNotiCycle = contents.parkingSpotStatusnotiCycle!!.toInt() }
//        if (!parkSiteInfoService.saveParkSiteInfo(parkSite!!)) {
//            tmapSendService.sendTmapInterface(reqSendResultResponse(result = "FAIL"), request.requestId!!, "profileSetupResponse")
//        }

        // gate update
        contents.gateList!!.forEach { gate ->
            parkinglotService.getGateInfoByUdpGateId(gate.gateId)?.let {
                it.takeAction = gate.takeAction
                it.seasonTicketTakeAction = gate.seasonTicketTakeAction
                it.whiteListTakeAction = gate.whiteListTakeAction
                if (!parkinglotService.saveGate(it)) {
                    tmapSendService.sendTmapInterface(
                        reqSendResultResponse(result = "FAIL"),
                        request.requestId!!,
                        "profileSetupResponse"
                    )
                }
            }
        }

        // display_message update
        contents.messageList?.let {
            val messages = ArrayList<reqSetDisplayMessage>()
            contents.messageList!!.forEach { message ->
                val new = reqSetDisplayMessage(
                    messageClass = DisplayMessageClass.IN,
                    messageType = when (message.messageType) {
                        "NONMEMBER" -> DisplayMessageType.NONMEMBER
                        "VIP" -> DisplayMessageType.VIP
                        "MEMBER" -> DisplayMessageType.MEMBER
                        else -> DisplayMessageType.NONMEMBER
                    },
                    line = 1,
                    order = 1,
                    colorCode = "C1",
                    messageDesc = message.message!!
                )
                messages.add(new)
            }
            if (facilityService.setDisplayMessage(messages).code == ResultCode.VALIDATE_FAILED.getCode()){
                tmapSendService.sendTmapInterface(reqSendResultResponse(result = "FAIL"), request.requestId!!, "profileSetupResponse")
            }
        }
        tmapSendService.sendTmapInterface(reqSendResultResponse(result = "SUCCESS"), request.requestId!!, "profileSetupResponse")
    }

    fun commandGateTakeActionSetup(request: reqApiTmapCommon) {
        val contents = readValue(request.contents.toString(), reqCommandGateTakeActionSetup::class.java)

        try {
//            contents.gateList.forEach { gate ->
//                val ticketType = when (gate.takeActionType) {
//                    "whiteList" -> TicketType.WHITELIST
//                    "seasonTicket" -> TicketType.SEASONTICKET
//                    else -> TicketType.ETC
//                }
//                when (gate.setupOption) {
//                    SetupOption.ADD -> {
//                        gate.vehicleList.forEach { vehicle ->
//                            if (!productService.createProduct(
//                                    reqCreateProduct(
//                                        vehicleNo = vehicle.vehicleNumber, userId = vehicle.messageType,
//                                        effectDate = DateUtil.stringToLocalDateTime(vehicle.startDateTime),
//                                        expireDate = DateUtil.stringToLocalDateTime(vehicle.endDateTime),
//                                        gateId = mutableSetOf(parkinglotService.getGateInfoByUdpGateId(gate.gateId)!!.gateId),
//                                        ticketType = ticketType
//                                    )
//                                )
//                            ) {
//                                logger.error { "createProduct is failed" }
//                            }
//                        }
//                    }
//                    SetupOption.UPDATE -> {
//                    }
//                    else -> {
//
//                    }
//                }
//
//            }
        } catch (e: RuntimeException) {
            logger.error { "commandGateTakeActionSetup is failed ${e.message}" }
            tmapSendService.sendTmapInterface(
                reqSendResultResponse(result = "SUCCESS"),
                request.requestId!!,
                "gateTakeActionSetupResponse"
            )
        }
    }

    fun commandVehicleListSearch(request: reqApiTmapCommon) {
        val contents = readValue(request.contents.toString(), reqCommandVehicleListSearchResponse::class.java)

        if (contents.vehicleList.isNullOrEmpty()) {
            //
            // todo 출차 전광판에 에러 표기
        }

    }

    fun commandInOutVehicleInformationSetup(request: reqApiTmapCommon) {
        val contents = readValue(request.contents.toString(), reqInOutVehicleInformationSetup::class.java)
        try {
            inoutService.modifyInOutVehicleByTmap(contents, request.requestId!!)
        }catch (e: RuntimeException){
            logger.error { "commandInOutVehicleInformationSetup is failed ${e.message}" }
        }

    }

}