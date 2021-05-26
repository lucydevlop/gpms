package io.glnt.gpms.schedule

import mu.KLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class ScheduledTasks {
    companion object : KLogging()

    /* 10s */
    @Scheduled(initialDelay = 10000, fixedRate = 10000)
    fun rcsJob() {
        logger.info("postJob, rest")
    }
}