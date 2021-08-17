package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.StatisticsInoutByDayDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketAplyType
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StatisticsService(
    private val parkInRepository: ParkInRepository,
    private val parkOutRepository: ParkOutRepository
) {
    companion object : KLogging()

    fun getInoutByDays(startDate: String, endDate: String): List<StatisticsInoutByDayDTO> {
        var day = DateUtil.stringToLocalDate(startDate)
        val result = ArrayList<StatisticsInoutByDayDTO>()
        var ticketCode : List<String> = listOf("NORMAL", "UNRECOGNIZED")
        while (day <= DateUtil.stringToLocalDate(endDate)) {
            val ins = parkInRepository.findByInDateBetweenAndDelYnAndOutSnGreaterThan(
                DateUtil.beginTimeToLocalDateTime(day.toString()),
                DateUtil.lastTimeToLocalDateTime(day.toString()), DelYn.N, -1)

            val outs = parkOutRepository.findByOutDateBetweenAndDelYn(
                DateUtil.beginTimeToLocalDateTime(day.toString()),
                DateUtil.lastTimeToLocalDateTime(day.toString()), DelYn.N
            )

            result.add(StatisticsInoutByDayDTO(
                        date = day.toString(),
                        inCnt = ins?.size ?: kotlin.run { null },
                        normalCnt = ins?.let { it.filter { it.parkcartype == "NORMAL" }.size },
                        unrecognizedCnt = ins?.let { it.filter { it.parkcartype == "UNRECOGNIZED" }.size },
                        ticketCnt = ins?.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size },
                        outCnt = outs?.size ?: kotlin.run { null },
                        parkFee = outs?.sumOf { it.parkfee?: kotlin.run { 0 }},
                        discountFee = outs?.sumOf { it.discountfee?: kotlin.run { 0 }},
                        dayDiscountFee = outs?.sumOf { it.dayDiscountfee?: kotlin.run { 0 }},
                        payFee = outs?.sumOf { it.payfee?: kotlin.run { 0 }}
            ))

            day = day.plusDays(1);

        }
        return result
    }

}