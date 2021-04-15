package io.glnt.gpms.handler.holiday.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.repository.HolidayRepository
import io.glnt.gpms.model.enums.DelYn
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class HolidayService {
    companion object : KLogging()

    @Autowired
    private lateinit var holidayRepository: HolidayRepository

    fun isHolidayByLocalDate(request: LocalDate) : Boolean {
        logger.debug { "isHoliday $request" }
        holidayRepository.findByHolidateAndDelYn(request, DelYn.N)?.let {
            return true
        }
        return false
    }

    fun isHolidayByLocalDateTime(request: LocalDateTime) : Boolean {
        logger.debug { "isHoliday $request" }
        holidayRepository.findByHolidateAndDelYn(request.toLocalDate(), DelYn.N)?.let {
            return true
        }
        return false
    }

    fun isHolidayByString(request: String) : Boolean {
        logger.debug { "isHoliday $request" }
        holidayRepository.findByHolidateAndDelYn(DateUtil.stringToLocalDate(request), DelYn.N)?.let {
            return true
        }
        return false
    }
}