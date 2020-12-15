package io.glnt.gpms.handler.facility.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.facility.model.DisplayLine
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqSetDisplayColor
import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import io.glnt.gpms.model.repository.DisplayColorRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class FacilityService {
    companion object : KLogging()

    @Value("\${gateway.url}")
    lateinit var url: String

    @Autowired
    private lateinit var displayColorRepository: DisplayColorRepository

    fun openGate(facilityId: String) {
        logger.debug { "openGate request ${facilityId}" }
        try {

        } catch (e: RuntimeException) {
            logger.error {  "openGate ${facilityId} error ${e.message}"}
        }
    }

    fun displayOutGate(facilityId: String, line1: String, line2: String) {
        val data = reqDisplayMessage(
            line1 = DisplayLine(color = displayColorRepository.findByPositionAndType(DisplayPosition.OUT, DisplayType.NORMAL1)!!.colorCode,
                                text = line1),
            line2 = DisplayLine(color = displayColorRepository.findByPositionAndType(DisplayPosition.OUT, DisplayType.NORMAL2)!!.colorCode,
                                text = line2)
        )
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
}