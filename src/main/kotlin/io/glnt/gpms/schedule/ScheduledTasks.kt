package io.glnt.gpms.schedule

import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.rcs.service.RcsService
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks(
    private var facilityService: FacilityService,
    private var parkinglotService: ParkinglotService,
    private var rcsService: RcsService
) {
    companion object : KLogging()

    /* 100s */
    @Scheduled(initialDelay = 100000, fixedRate = 100000)
    fun rcsJob() {
        logger.info("postJob, rest")
        if (parkinglotService.isExternalSend()){
            facilityService.activeGateFacilities()?.let { it ->
                rcsService.asyncFacilitiesHealth(it)
            }
        }
    }
}