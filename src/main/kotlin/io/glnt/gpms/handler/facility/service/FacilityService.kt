package io.glnt.gpms.handler.facility.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException

import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqSendDisplay
import io.glnt.gpms.handler.facility.model.reqSetDisplayColor
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.tmap.model.reqSendVehicleListSearch
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.handler.vehicle.service.VehicleService
import io.glnt.gpms.model.entity.VehicleListSearch
import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import io.glnt.gpms.model.repository.DisplayColorRepository
import io.glnt.gpms.model.repository.DisplayMessageRepository
import io.glnt.gpms.model.repository.VehicleListSearchRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class FacilityService {
    companion object : KLogging()

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
    private lateinit var vehicleService: VehicleService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var restAPIManager: RestAPIManagerUtil

    @Autowired
    private lateinit var vehicleListSearchRepository: VehicleListSearchRepository

    fun openGate(id: String, type: String) {
        logger.debug { "openGate request ${type} ${id}" }
        try {
            when (type) {
                "GATE" -> {
                    parkinglotService.getFacilityByGateAndCategory(id, "BREAKER")?.let { its ->
                        its.forEach {
                            restAPIManager.sendPostRequest(
                                "$url/v1/breaker/${it.facilitiesId}/open",
                                null
                            )
                        }
                    }
                }
                else -> {
                    restAPIManager.sendPostRequest(
                        "$url/v1/breaker/${id}/open",
                        null
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
        try {
            request.forEach { it ->
                displayColorRepository.findByPositionAndType(it.position!!, it.type!!)?.let { displayColor ->
                    displayColor.colorCode = it.colorCode
                    displayColor.colorDesc = it.colorDesc
                    displayColorRepository.save(displayColor)
                } ?: run {
                    displayColorRepository.save(
                        DisplayColor( sn = null,
                                      position = it.position!!, type = it.type!!,
                                      colorCode = it.colorCode, colorDesc = it.colorDesc ))
                }
            }
            return CommonResult.created("parkinglot display setting success")

        } catch (e: RuntimeException) {
            logger.error("set display color error {} ", e.message)
            return CommonResult.error("parkinglot display setting failed ")
        }
    }

    fun setDisplayMessage(request: ArrayList<reqSetDisplayMessage>): CommonResult = with(request) {
        try {
            request.forEach { it ->
                displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(it.messageClass!!, it.messageType!!, it.order)?.let { displayMessage ->
                    displayMessage.colorType = it.colorType
                    displayMessage.messageDesc = it.messageDesc
                    displayMessage.lineNumber = it.line
                    displayMessageRepository.save(displayMessage)
                } ?: run {
                    displayMessageRepository.save(
                        DisplayMessage( sn = null,
                            messageClass = it.messageClass!!, messageType = it.messageType!!,
                            colorType = it.colorType, order = it.order, messageDesc = it.messageDesc, lineNumber = it.line ))
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

    fun sendDisplayMessage(data: Any, gate: String) {
        parkinglotService.getFacilityByGateAndCategory(gate, "DISPLAY")?.let { its ->
            its.forEach {
                restAPIManager.sendPostRequest(
                    url,
                    reqSendDisplay(it.facilitiesId!!, data as ArrayList<reqDisplayMessage>)
                )
            }
        }
    }

    fun sendPaystation(data: Any) {
        //todo 정산기 api 연계 개발
    }

    @Throws(CustomException::class)
    fun searchCarNumber(request: reqSendVehicleListSearch): CommonResult? {
        if (tmapSend == "on") {
            // table db insert
            val requestId = DataCheckUtil.generateRequestId(parkinglotService.parkSiteId())
            vehicleListSearchRepository.save(VehicleListSearch(requestId = requestId, facilityId = request.facilityId))
            tmapSendService.sendVehicleListSearch(request, requestId)
        } else {
            return vehicleService.searchParkInByVehicleNo(request.vehicleNumber)
        }
        return null
    }
}