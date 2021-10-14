package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Holiday
import io.glnt.gpms.model.enums.DelYn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
interface HolidayRepository: JpaRepository<Holiday, Long>, JpaSpecificationExecutor<Holiday> {
    fun findByStartDateAndEndDateAndDelYn(startDate: LocalDate, endDate: LocalDate, delYn: DelYn): Optional<Holiday>
    fun findByStartDateAndEndDateAndIsWorkingAndDelYn(startDate: LocalDate, endDate: LocalDate, isWorking: Boolean, delYn: DelYn): Optional<Holiday>
    fun findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndDelYn(startDate: LocalDate, endDate: LocalDate, delYn: DelYn): List<Holiday>
}