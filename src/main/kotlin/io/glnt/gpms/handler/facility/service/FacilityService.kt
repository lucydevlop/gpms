package io.glnt.gpms.handler.facility.service

import io.glnt.gpms.common.api.CommonResult

import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqSetDisplayColor
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import io.glnt.gpms.model.repository.DisplayColorRepository
import io.glnt.gpms.model.repository.DisplayMessageRepository
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

    @Autowired
    private lateinit var displayColorRepository: DisplayColorRepository

    @Autowired
    private lateinit var displayMessageRepository: DisplayMessageRepository

    fun openGate(facilityId: String) {
        logger.debug { "openGate request ${facilityId}" }
        try {

        } catch (e: RuntimeException) {
            logger.error {  "openGate ${facilityId} error ${e.message}"}
        }
    }

    fun displayInGate(facilityId: String, messages: ArrayList<reqDisplayMessage>) {
//        val data = reqDisplayMessage(
//            line1 = DisplayLine(color = displayColorRepository.findByPositionAndType(DisplayPosition.IN, DisplayType.NORMAL1)!!.colorCode,
//                text = line1),
//            line2 = DisplayLine(color = displayColorRepository.findByPositionAndType(DisplayPosition.OUT, DisplayType.NORMAL2)!!.colorCode,
//                text = line2)
//        )
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

    fun sendDisplayMessage(data: Any) {

    }

    fun sendPaystation(data: Any) {
        //todo 정산기 api 연계 개발
    }

}