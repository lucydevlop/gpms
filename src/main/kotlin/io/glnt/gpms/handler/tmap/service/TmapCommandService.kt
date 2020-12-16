package io.glnt.gpms.handler.tmap.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.facility.model.reqParkingSiteInfo
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

    fun getRequestCommand(request: reqApiTmapCommon) {
        // db insert
        tmapCommandRepository.save(
            TmapCommand(sn = null,
                        parkingSiteId = request.parkingSiteId,
                        type = request.type,
                        responseId = request.responseId?.run { request.requestId },
                        eventDateTime = request.commandDateTime?.run { request.eventDateTime },
                        contents = request.contents.toString()))
        when(request.type) {
            "parkingsiteinfo" ->
                { commandParkingSiteInfo() }
            "dspcolorinfo" ->
                { facilityService.fetchDisplayColor() }
            "facilitiesRegistResponse" -> {}
            "facilitiesCommand" -> {
                commandFacilities(request)
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
            type="ParkingInfo", parkingSiteId = parkinglotService.parkSite.siteid,
            requestId = DateUtil.stringToNowDateTime(), eventDateTime = DateUtil.stringToNowDateTime(),
            contents = contents )

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