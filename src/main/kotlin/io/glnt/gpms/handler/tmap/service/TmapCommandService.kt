package io.glnt.gpms.handler.tmap.service

import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.handler.tmap.model.reqCommandFacilities
import io.glnt.gpms.model.entity.TmapCommand
import io.glnt.gpms.model.repository.TmapCommandRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TmapCommandService {
    companion object : KLogging()

    @Autowired
    private lateinit var tmapCommandRepository: TmapCommandRepository

    @Autowired
    private lateinit var facilityService: FacilityService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    fun getRequestCommand(request: reqApiTmapCommon) : Any {
        // db insert
        tmapCommandRepository.save(
            TmapCommand(sn = null,
                        parkingSiteId = request.parkingSiteId,
                        type = request.type,
                        responseId = request.responseId?.run { request.requestId },
                        eventDateTime = request.commandDateTime?.run { request.eventDateTime },
                        contents = request.contents.toString()))
        return request.contents
    }

    /* request 'facilitiesCommand' */
    fun commandFacilities(request: reqApiTmapCommon) {
        val contents : reqCommandFacilities = getRequestCommand(request) as reqCommandFacilities
        // todo facilityid check
        contents.BLOCK?.let { it ->
            when(it) {
                "OPEN" -> {
                    // gate
                    facilityService.openGate(contents.facilitiesId)
                    // display
                    val facility = parkinglotService.getFacility(contents.facilitiesId)
                    facilityService.displayOutGate(facility!!.gateId, "감사합니다", "안녕히가세요")
                }
                "CLOSE" -> {}
                "OPENLOCKING" -> {}
                "RELEASE" -> {}
                else -> {}
            }

        }

    }

}