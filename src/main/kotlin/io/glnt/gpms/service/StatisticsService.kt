package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.StatisticsInoutCountDTO
import io.glnt.gpms.model.dto.StatisticsInoutDTO
import io.glnt.gpms.model.dto.StatisticsInoutPaymentDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.mapper.StatisticsInoutMapper
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class StatisticsService(
    private val parkInRepository: ParkInRepository,
    private val parkOutRepository: ParkOutRepository,
    private val statisticsInoutMapper: StatisticsInoutMapper
) {
    companion object : KLogging()

    val ticketCode : List<String> = listOf("NORMAL", "UNRECOGNIZED")

    fun getInoutCountByDays(startDate: String, endDate: String): List<StatisticsInoutCountDTO> {
        var day = DateUtil.stringToLocalDate(startDate)
        val statisticsInoutCountsDTO = ArrayList<StatisticsInoutCountDTO>()
//        val ticketCode : List<String> = listOf("NORMAL", "UNRECOGNIZED")
        while (day <= DateUtil.stringToLocalDate(endDate)) {
            val parkIns = parkInRepository.findByInDateBetweenAndDelYnAndOutSnGreaterThan(
                DateUtil.beginTimeToLocalDateTime(day.toString()),
                DateUtil.lastTimeToLocalDateTime(day.toString()), DelYn.N, -1)

            val parkOuts = parkOutRepository.findByOutDateBetweenAndDelYn(
                DateUtil.beginTimeToLocalDateTime(day.toString()),
                DateUtil.lastTimeToLocalDateTime(day.toString()), DelYn.N)

            statisticsInoutCountsDTO.add(
                StatisticsInoutCountDTO(
                    date = day.toString(),
                    inCnt = parkIns?.size,
                    inNormalCnt = parkIns?.let { it.filter { it.parkcartype == "NORMAL" }.size },
                    inUnrecognizedCnt = parkIns?.let { it.filter { it.parkcartype!!.contains("RECOGNIZED") }.size },
                    inTicketCnt = parkIns?.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size },
                    outCnt = parkOuts?.size,
                    outNormalCnt = parkOuts?.let { it.filter { it.parkcartype == "NORMAL" }.size },
                    outUnrecognizedCnt = parkOuts?.let { it.filter { it.parkcartype!!.contains("RECOGNIZED") }.size },
                    outTicketCnt = parkOuts?.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size }

//                    parkFee = result.sumOf { it.parkFee?: kotlin.run { 0 }},
//                    discountFee = result.sumOf { it.discountFee?: kotlin.run { 0 }},
//                    dayDiscountFee = result.sumOf { it.dayDiscountFee?: kotlin.run { 0 }},
//                    payFee = result.sumOf { it.payFee?: kotlin.run { 0 }},
//                    nonPayment = result.sumOf { it.nonPayment?: kotlin.run { 0 }},
//                    payment = result.sumOf { it.payment?: kotlin.run { 0 }},
            ))

            day = day.plusDays(1);
        }
        return statisticsInoutCountsDTO
    }

    fun getInoutCountByMonths(startDate: String, endDate: String) : List<StatisticsInoutCountDTO> {
        var day = DateUtil.stringToLocalDate(startDate)
//        val ticketCode : List<String> = listOf("NORMAL", "UNRECOGNIZED")

        val statisticsInoutCountsDTO = ArrayList<StatisticsInoutCountDTO>()

        while (day <= DateUtil.stringToLocalDate(endDate)) {
            var result = ArrayList<StatisticsInoutDTO>()
            val parkIns = parkInRepository.findByInDateBetweenAndDelYnAndOutSnGreaterThan(
                DateUtil.firstDayToLocalDateTime(day.toString()),
                DateUtil.lastDayToLocalDateTime(day.toString()), DelYn.N, -1)

            val parkOuts = parkOutRepository.findByOutDateBetweenAndDelYn(
                DateUtil.firstDayToLocalDateTime(day.toString()),
                DateUtil.lastDayToLocalDateTime(day.toString()), DelYn.N)

            statisticsInoutCountsDTO.add(
                StatisticsInoutCountDTO(
                    date = day.toString(),
                    inCnt = parkIns?.size,
                    inNormalCnt = parkIns?.let { it.filter { it.parkcartype == "NORMAL" }.size },
                    inUnrecognizedCnt = parkIns?.let { it.filter { it.parkcartype!!.contains("RECOGNIZED") }.size },
                    inTicketCnt = parkIns?.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size },
                    outCnt = parkOuts?.size,
                    outNormalCnt = parkOuts?.let { it.filter { it.parkcartype == "NORMAL" }.size },
                    outUnrecognizedCnt = parkOuts?.let { it.filter { it.parkcartype!!.contains("RECOGNIZED") }.size },
                    outTicketCnt = parkOuts?.let { it.filterNot { ticketCode.contains(it.parkcartype)  }.size }
                )
            )
            day = day.plusMonths(1)
        }

        return statisticsInoutCountsDTO
    }

    fun getInoutPaymentByDays(startDate: String, endDate: String): List<StatisticsInoutPaymentDTO> {
        var day = DateUtil.stringToLocalDate(startDate)

        val statisticsInoutPaymentsDTO = ArrayList<StatisticsInoutPaymentDTO>()

        while (day <= DateUtil.stringToLocalDate(endDate)) {
            var result = ArrayList<StatisticsInoutDTO>()

            parkOutRepository.findByOutDateBetweenAndDelYn(
                DateUtil.beginTimeToLocalDateTime(day.toString()),
                DateUtil.lastTimeToLocalDateTime(day.toString()), DelYn.N)?.let { parkOuts ->

                parkOuts.forEach { parkOut ->
                    result.add(statisticsInoutMapper.toDTO(parkOut))
                }
            }

            statisticsInoutPaymentsDTO.add(
                StatisticsInoutPaymentDTO(
                    date = day.toString(),
                    parkFee = result.sumOf { it.parkFee ?: 0 },        // result.let { parkOut -> parkOut.sumOf { it.parkfee ?: 0 } },
                    discountFee = result.sumOf { it.discountFee ?: 0 }, // parkOuts?.let { parkOut -> parkOut.sumOf { it.discountfee ?: 0 } },
                    dayDiscountFee = result.sumOf { it.dayDiscountFee ?: 0 }, // parkOuts?.let { parkOut -> parkOut.sumOf { it.dayDiscountFee ?: 0 } },
                    payFee = result.sumOf { it.payFee ?: 0 }, //  parkOuts?.let { parkOut -> parkOut.sumOf { it.payfee ?: 0 } },
                    nonPayment = result.sumOf { it.nonPayment ?: 0 },
                    payment = result.sumOf { it.payment ?: 0 },
                ))

            day = day.plusDays(1);
        }
        return statisticsInoutPaymentsDTO
    }

    fun getInoutPaymentByMonths(startDate: String, endDate: String): List<StatisticsInoutPaymentDTO> {
        var day = DateUtil.stringToLocalDate(startDate)

        val statisticsInoutPaymentsDTO = ArrayList<StatisticsInoutPaymentDTO>()

        while (day <= DateUtil.stringToLocalDate(endDate)) {
            var result = ArrayList<StatisticsInoutDTO>()

            parkOutRepository.findByOutDateBetweenAndDelYn(
                DateUtil.firstDayToLocalDateTime(day.toString()),
                DateUtil.lastDayToLocalDateTime(day.toString()), DelYn.N)?.let { parkOuts ->

                parkOuts.forEach { parkOut ->
                    result.add(statisticsInoutMapper.toDTO(parkOut))
                }
            }

            statisticsInoutPaymentsDTO.add(
                StatisticsInoutPaymentDTO(
                    date = day.toString(),
                    parkFee = result.sumOf { it.parkFee ?: 0 },        // result.let { parkOut -> parkOut.sumOf { it.parkfee ?: 0 } },
                    discountFee = result.sumOf { it.discountFee ?: 0 }, // parkOuts?.let { parkOut -> parkOut.sumOf { it.discountfee ?: 0 } },
                    dayDiscountFee = result.sumOf { it.dayDiscountFee ?: 0 }, // parkOuts?.let { parkOut -> parkOut.sumOf { it.dayDiscountFee ?: 0 } },
                    payFee = result.sumOf { it.payFee ?: 0 }, //  parkOuts?.let { parkOut -> parkOut.sumOf { it.payfee ?: 0 } },
                    nonPayment = result.sumOf { it.nonPayment ?: 0 },
                    payment = result.sumOf { it.payment ?: 0 },
                ))

            day = day.plusMonths(1)
        }
        return statisticsInoutPaymentsDTO
    }

}