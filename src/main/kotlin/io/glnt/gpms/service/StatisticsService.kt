package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.StatisticsInoutByDayDTO
import io.glnt.gpms.model.dto.StatisticsInoutByMonthDTO
import io.glnt.gpms.model.dto.StatisticsInoutDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.enums.TicketAplyType
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.mapper.StatisticsInoutMapper
import io.glnt.gpms.model.repository.InoutPaymentRepository
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class StatisticsService(
    private val parkInRepository: ParkInRepository,
    private val parkOutRepository: ParkOutRepository,
    private val statisticsInoutMapper: StatisticsInoutMapper
) {
    companion object : KLogging()

    fun getInoutByDays(startDate: String, endDate: String): List<StatisticsInoutByDayDTO> {
        var day = DateUtil.stringToLocalDate(startDate)
        val statisticsInoutByDayDTO = ArrayList<StatisticsInoutByDayDTO>()
        val ticketCode : List<String> = listOf("NORMAL", "UNRECOGNIZED")
        while (day <= DateUtil.stringToLocalDate(endDate)) {
            val result = ArrayList<StatisticsInoutDTO>()
            parkInRepository.findByInDateBetweenAndDelYnAndOutSnGreaterThan(
                DateUtil.beginTimeToLocalDateTime(day.toString()),
                DateUtil.lastTimeToLocalDateTime(day.toString()), DelYn.N, -1)?.let { parkIns ->

                parkIns.forEach { parkIn ->
                    result.add(statisticsInoutMapper.toDTO(parkIn))
                }
            }

            statisticsInoutByDayDTO.add(
                StatisticsInoutByDayDTO(
                    date = day.toString(),
                    inCnt = result.size,
                    normalCnt = result.let { it.filter { it.parkcartype == "NORMAL" }.size },
                    unrecognizedCnt = result.let { it.filter { it.parkcartype == "UNRECOGNIZED" }.size },
                    ticketCnt = result.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size },
                    outCnt = result.sumOf { it.out?: kotlin.run { 0 }},
                    parkFee = result.sumOf { it.parkFee?: kotlin.run { 0 }},
                    discountFee = result.sumOf { it.discountFee?: kotlin.run { 0 }},
                    dayDiscountFee = result.sumOf { it.dayDiscountFee?: kotlin.run { 0 }},
                    payFee = result.sumOf { it.payFee?: kotlin.run { 0 }},
                    nonPayment = result.sumOf { it.nonPayment?: kotlin.run { 0 }},
                    payment = result.sumOf { it.payment?: kotlin.run { 0 }},
            ))

            day = day.plusDays(1);
        }
        return statisticsInoutByDayDTO
    }

    fun getInoutByMonths(startDate: String, endDate: String) : List<StatisticsInoutByMonthDTO> {
        var day = DateUtil.stringToLocalDate(startDate)
        val ticketCode : List<String> = listOf("NORMAL", "UNRECOGNIZED")

        val statisticsInoutByMonthDTO = ArrayList<StatisticsInoutByMonthDTO>()

        while (day <= DateUtil.stringToLocalDate(endDate)) {
            var result = ArrayList<StatisticsInoutDTO>()
            parkInRepository.findByInDateBetweenAndDelYnAndOutSnGreaterThan(
                DateUtil.firstDayToLocalDateTime(day.toString()),
                DateUtil.lastDayToLocalDateTime(day.toString()), DelYn.N, -1)?.let { parkIns ->

                parkIns.forEach { parkIn ->
                    result.add(statisticsInoutMapper.toDTO(parkIn))
                }
            }
            statisticsInoutByMonthDTO.add(
                StatisticsInoutByMonthDTO(
                    date = day.toString(),
                    inCnt = result.size,
                    normalCnt = result.let { it.filter { it.parkcartype == "NORMAL" }.size },
                    unrecognizedCnt = result.let { it.filter { it.parkcartype == "UNRECOGNIZED" }.size },
                    ticketCnt = result.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size },
                    outCnt = result.sumOf { it.out?: kotlin.run { 0 }},
                    parkFee = result.sumOf { it.parkFee?: kotlin.run { 0 }},
                    discountFee = result.sumOf { it.discountFee?: kotlin.run { 0 }},
                    dayDiscountFee = result.sumOf { it.dayDiscountFee?: kotlin.run { 0 }},
                    payFee = result.sumOf { it.payFee?: kotlin.run { 0 }},
                    nonPayment = result.sumOf { it.nonPayment?: kotlin.run { 0 }},
                    payment = result.sumOf { it.payment?: kotlin.run { 0 }},
                )
            )
            day = day.plusMonths(1)
        }

        return statisticsInoutByMonthDTO
    }

}