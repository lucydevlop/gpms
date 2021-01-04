package io.glnt.gpms.handler.tmap.service

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JSONUtil
import io.glnt.gpms.handler.facility.model.reqParkingSiteInfo
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.model.reqCreateProduct
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.vehicle.service.VehicleService
import io.glnt.gpms.model.entity.TmapCommand
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.TmapCommandRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList


@Service
class TmapCommandService {
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
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var vehicleService: VehicleService

    fun getRequestCommand(request: reqApiTmapCommon) {
        request.contents = JSONUtil.getJsObject(request.contents)
        // db insert
        tmapCommandRepository.save(
            TmapCommand(
                sn = null,
                parkingSiteId = request.parkingSiteId,
                type = request.type,
                responseId = request.responseId?.run { request.requestId },
                eventDateTime = request.commandDateTime?.run { request.eventDateTime },
                contents = request.contents.toString()
            )
        )
        val data = JSONUtil.getJsObject(request.contents)

        when(request.type) {
            "parkingsiteinfo" -> {
                commandParkingSiteInfo()
            }
            "dspcolorinfo" -> {
                facilityService.fetchDisplayColor()
            }
            "profileSetup" -> {
                commandProfileSetup(request)
            }
            "facilitiesRegistResponse" -> {
            }
            "facilitiesCommand" -> {
                commandFacilities(request)
            }
            "gateTakeActionSetup" -> {
                commandGateTakeActionSetup(request)
            }
            "vehicleListSearchResponse" -> {
                commandVehicleListSearch(request)
            }
            "inOutVehicleInformationSetup" -> {
                commandInOutVehicleInformationSetup(request)
            }
            else -> {}
        }
    }

    fun commandParkingSiteInfo() {
        val contents = reqParkingSiteInfo(
            parkingSiteName = parkinglotService.parkSite.sitename,
            lotNumberAddress = "--",
            roadNameAddress = parkinglotService.parkSite.address!!,
            detailsAddress = "**",
            telephoneNumber = parkinglotService.parkSite.tel!!,
            saupno = parkinglotService.parkSite.saupno!!,
            businessName = parkinglotService.parkSite.ceoname!!
        )
        val data = reqApiTmapCommon(
            type = "ParkingInfo", parkingSiteId = parkinglotService.parkSite.siteid,
            requestId = DateUtil.stringToNowDateTime(), eventDateTime = DateUtil.stringToNowDateTime(),
            contents = contents
        )

        facilityService.sendPaystation(data)
    }

    /* request 'facilitiesCommand' */
    fun commandFacilities(request: reqApiTmapCommon) {
        val contents : reqCommandFacilities = request.contents as reqCommandFacilities
        // todo facilityid check
        contents.BLOCK?.let { it ->
            when(it) {
                "OPEN" -> {
                    // gate
                    facilityService.openGate(contents.facilitiesId, "FACILITY")
                    // display
                    val facility = parkinglotService.getFacility(contents.facilitiesId)
                    facilityService.displayOutGate(facility!!.gateId, "감사합니다", "안녕히가세요")
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

        // parksite update
        contents.facilitiesStatusNotiCycle?.let {  parkinglotService.parkSite.facilitiesStatusNotiCycle = contents.facilitiesStatusNotiCycle!!.toInt() }
        contents.parkingSpotStatusnotiCycle?.let {  parkinglotService.parkSite.parkingSpotStatusNotiCycle = contents.parkingSpotStatusnotiCycle!!.toInt() }
        if (!parkinglotService.saveParkSiteInfo(parkinglotService.parkSite)) {
            tmapSendService.sendProfileSetupResponse(reqSendResultResponse(result = "FAIL"), request.requestId!!)
        }

        // gate update
        contents.gateList!!.forEach { gate ->
            parkinglotService.getGateInfoByUdpGateId(gate.gateId)?.let {
                it.takeAction = gate.takeAction
                it.seasonTicketTakeAction = gate.seasonTicketTakeAction
                it.whiteListTakeAction = gate.whiteListTakeAction
                if (!parkinglotService.saveGate(it)) {
                    tmapSendService.sendProfileSetupResponse(
                        reqSendResultResponse(result = "FAIL"),
                        request.requestId!!
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
                    colorType = DisplayType.NORMAL1,
                    messageDesc = message.message!!
                )
                messages.add(new)
            }
            if (facilityService.setDisplayMessage(messages).code == ResultCode.VALIDATE_FAILED.getCode()){
                tmapSendService.sendProfileSetupResponse(reqSendResultResponse(result = "FAIL"), request.requestId!!)
            }
        }
        tmapSendService.sendProfileSetupResponse(reqSendResultResponse(result = "SUCCESS"), request.requestId!!)
    }

    fun commandGateTakeActionSetup(request: reqApiTmapCommon) {
        val contents = readValue(request.contents.toString(), reqCommandGateTakeActionSetup::class.java)

        try {
            contents.gateList.forEach { gate ->
                val ticketType = when (gate.takeActionType) {
                    "whiteList" -> TicketType.WHITELIST
                    "seasonTicket" -> TicketType.SEASONTICKET
                    else -> TicketType.ETC
                }
                when (gate.setupOption) {
                    SetupOption.ADD -> {
                        gate.vehicleList.forEach { vehicle ->
                            if (!productService.createProduct(
                                    reqCreateProduct(
                                        vehicleNo = vehicle.vehicleNumber, userId = vehicle.messageType,
                                        startDate = DateUtil.stringToLocalDateTime(vehicle.startDateTime),
                                        endDate = DateUtil.stringToLocalDateTime(vehicle.endDateTime),
                                        gateId = mutableSetOf(parkinglotService.getGateInfoByUdpGateId(gate.gateId)!!.gateId),
                                        ticktType = ticketType
                                    )
                                )
                            ) {
                                logger.error { "createProduct is failed" }
                            }
                        }
                    }
                    SetupOption.UPDATE -> {
                    }
                    else -> {

                    }
                }

            }
        } catch (e: RuntimeException) {
            logger.error { "createProduct is success" }
            tmapSendService.sendGateTakeActionSetupResponse(
                reqSendResultResponse(result = "SUCCESS"),
                request.requestId!!
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
        val content = readValue(request.contents.toString(), reqInOutVehicleInformationSetup::class.java)
        vehicleService.modifyInOutVehicleByTmap(content, request.requestId!!)
    }


    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
        val factory = JsonFactory()
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        return jacksonObjectMapper().readValue(any, valueType)
    }
}