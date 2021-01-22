package io.glnt.gpms.handler.relay.service

import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.tmap.service.TmapSendService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RelayService {
    companion object : KLogging()

    @Autowired
    private lateinit var tmapSendService: TmapSendService

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var facilityService: FacilityService

    fun facilitiesHealthCheck(request: reqRelayHealthCheck) {
        if (parkinglotService.parkSite.tmapSend == "ON")
            tmapSendService.sendHealthCheckRequest(request, "")

        request.facilitiesList.forEach { facility ->
            facilityService.updateHealthCheck(facility.facilitiesId, facility.healthStatus!!)
        }
    }
}