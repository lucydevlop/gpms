package io.glnt.gpms.service

import io.glnt.gpms.model.dto.HolidayDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.mapper.HolidayMapper
import io.glnt.gpms.model.repository.HolidayRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class HolidayService(
    private val holidayRepository: HolidayRepository,
    private val holidayMapper: HolidayMapper
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<HolidayDTO> {
        return holidayRepository.findAll().map(holidayMapper::toDto)
    }

    fun save(holidayDTO: HolidayDTO): HolidayDTO {
        logger.debug { "Request to save Holiday : $holidayDTO" }
        val holiday = holidayMapper.toEntity(holidayDTO)
        holidayRepository.saveAndFlush(holiday!!)
        return holidayMapper.toDto(holiday)
    }

    fun findByDays(startDate: LocalDate, endDate: LocalDate): MutableList<HolidayDTO> {
        return holidayRepository.findByStartDateGreaterThanEqualAndEndDateLessThanEqualAndDelYn(startDate, endDate, DelYn.N).mapTo(
            mutableListOf(), holidayMapper::toDto)
    }

    fun isHolidayByDay(date: LocalDate): Boolean {
        logger.debug { "$date holiday check" }
        val holiday = holidayRepository.findByStartDateAndEndDateAndIsWorkingAndDelYn(date, date, false, DelYn.N)
        return holiday.isPresent
    }
}