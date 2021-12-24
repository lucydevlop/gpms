package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Holiday
import io.glnt.gpms.model.enums.YN
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
interface HolidayRepository: JpaRepository<Holiday, Long>, JpaSpecificationExecutor<Holiday> {
    fun findByStartDateAndEndDateAndDelYn(startDate: LocalDateTime, endDate: LocalDateTime, delYn: YN): Optional<Holiday>
    fun findByStartDateAndEndDateAndIsWorkingAndDelYn(startDate: LocalDateTime, endDate: LocalDateTime, isWorking: Boolean, delYn: YN): Optional<Holiday>
    fun findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndDelYn(startDate: LocalDateTime, endDate: LocalDateTime, delYn: YN): List<Holiday>
    fun findByStartDateLessThanEqualAndEndDateGreaterThanEqualAndDelYn(startDate: LocalDateTime, endDate: LocalDateTime, delYn: YN): List<Holiday>
}