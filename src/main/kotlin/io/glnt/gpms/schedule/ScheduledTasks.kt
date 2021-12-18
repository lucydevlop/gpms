package io.glnt.gpms.schedule

import io.glnt.gpms.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.service.ParkSiteInfoService
import io.glnt.gpms.service.TicketService
import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks(
    private var facilityService: FacilityService,
    private var parkSiteInfoService: ParkSiteInfoService,
    private var rcsService: RcsService,
    private var seasonTicketService: TicketService
) {
    companion object : KLogging()

    /* 100s */
    @Scheduled(initialDelay = 100000, fixedRate = 100000)
    fun rcsJob() {
        logger.info("postJob, rest")
        if (parkSiteInfoService.isExternalSend()){
            facilityService.activeGateFacilities()?.let { it ->
                rcsService.asyncFacilitiesHealth(it)
            }
        }
    }

    /* 매일 23:00 시 */
    @Scheduled(cron = "0 0 23 * * *" , zone = "Asia/Seoul")
    fun batchExtendSeasonTicket() {
        logger.info("batchExtendSeasonTicket")
        seasonTicketService.extendSeasonTicket()
    }
}