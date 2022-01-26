package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.entity.SeasonTicketDTO
import io.glnt.gpms.model.enums.TicketAplyType
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.enums.WeekType
import io.glnt.gpms.model.mapper.SeasonTicketMapper
import io.glnt.gpms.model.repository.SeasonTicketRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.lang.Long.min
import java.time.LocalDateTime

@Service
class TicketService(
    private val seasonTicketMapper: SeasonTicketMapper,
    private val seasonTicketRepository: SeasonTicketRepository,
    private val corpService: CorpService,
    private val ticketClassService: TicketClassService
) {
    companion object : KLogging()

    fun saveTickets(ticketDTOs: ArrayList<SeasonTicketDTO>): List<SeasonTicketDTO> {
        logger.debug { "Request to save Tickets : $ticketDTOs" }
        val list = ArrayList<SeasonTicketDTO>()
        ticketDTOs.forEach { productTicketDTO ->
            productTicketDTO.corpName?.let {corpName ->
                corpService.getStoreByCorpName(corpName)?.let { corp ->
                    productTicketDTO.corpSn = corp.sn
                }
            }
            if (productTicketDTO.corpSn == 0L) productTicketDTO.corpSn = null
            val ticket = save(productTicketDTO)
            list.add(ticket)
        }
        return list
    }

    fun saveTicket(ticketDTO: SeasonTicketDTO): SeasonTicketDTO {
        logger.debug { "Request to save Ticket : $ticketDTO" }

        ticketDTO.corpName?.let { corpName ->
            corpService.getStoreByCorpName(corpName)?.let { corp ->
                ticketDTO.corpSn = corp.sn
            }
        }

        if (ticketDTO.corpSn == 0L) ticketDTO.corpSn = null
        return save(ticketDTO)
    }

    fun save(seasonTicketDTO: SeasonTicketDTO): SeasonTicketDTO {
        logger.debug { "Request to save Ticket : $seasonTicketDTO" }
        val ticket = seasonTicketMapper.toEntity(seasonTicketDTO)
        seasonTicketRepository.save(ticket!!)
        return seasonTicketMapper.toDTO(ticket)
    }

    fun getTicketByVehicleNoAndDate(vehicleNo: String, startDate: LocalDateTime, endDate: LocalDateTime): List<SeasonTicketDTO>? {
        return seasonTicketRepository.findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(vehicleNo, startDate, endDate,YN.N)
            ?.map(seasonTicketMapper::toDTO)
            ?: kotlin.run { null }
    }

    fun getTicketByVehicleNoAndTicketTypeAndRangeDate(vehicleNo: String, ticketType: TicketType, startDate: LocalDateTime, endDate: LocalDateTime): List<SeasonTicketDTO>? {
        return seasonTicketRepository.findByVehicleNoAndTicketTypeAndEffectDateBetweenAndDelYn(vehicleNo, ticketType, startDate, endDate, YN.N)
            ?.map(seasonTicketMapper::toDTO)
            ?: kotlin.run { null }
    }

    fun getLastTicketByVehicleNoAndTicketType(vehicleNo: String, ticketType: TicketType) : SeasonTicketDTO? {
        return if (ticketType == TicketType.ALL) seasonTicketRepository.findTopByVehicleNoAndDelYnOrderByExpireDateDesc(vehicleNo, YN.N)?.let { seasonTicket -> seasonTicketMapper.toDTO(seasonTicket) }
        else seasonTicketRepository.findTopByVehicleNoAndDelYnAndTicketTypeOrderByExpireDateDesc(vehicleNo, YN.N, ticketType)?.let { seasonTicket -> seasonTicketMapper.toDTO(seasonTicket) }
    }

    fun getValidTicket(vehicleNo: String, startTime: LocalDateTime, endTime: LocalDateTime): SeasonTicketDTO? {
        seasonTicketRepository.findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(vehicleNo, startTime, endTime, YN.N)?.let { tickets ->
            if (tickets.isEmpty()) return null

            tickets.forEach { ticket ->
                ticket.ticket?.let { ticketClass ->
                    // 유료 정기권 결제 정보 없을 시 false
                    if (((ticketClass.price?: 0) > 0) && ticket.payMethod == null) return null

                    if (DataCheckUtil.checkIncludeWeek(ticketClass.week!!, startTime)) {
                        when(ticketClass.aplyType) {
                            TicketAplyType.FULL -> return seasonTicketMapper.toDTO(ticket)
                            else -> {
                                val expireDate = if (ticketClass.startTime!! > ticketClass.endTime!!) {
                                    DateUtil.makeLocalDateTime(
                                        DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(startTime, 1)),
                                        ticketClass.endTime!!.substring(0, 2), ticketClass.endTime!!.substring(2, 4))
                                } else DateUtil.makeLocalDateTime(
                                    DateUtil.LocalDateTimeToDateString(startTime),
                                    ticketClass.endTime!!.substring(0, 2), ticketClass.endTime!!.substring(2, 4))

                                val effectDate = DateUtil.makeLocalDateTime(
                                    DateUtil.LocalDateTimeToDateString(startTime),
                                    ticketClass.startTime!!.substring(0, 2), ticketClass.startTime!!.substring(2, 4))

                                return if ( ( expireDate < startTime ) || (effectDate > endTime) )
                                    null
                                else
                                    seasonTicketMapper.toDTO(ticket)
                            }
                        }
                    } else {
                        return null
                    }
                }?: run {
                    return seasonTicketMapper.toDTO(ticket)
                }
            }
        }
        return null
    }

    fun calcRemainDayTicket(vehicleNo: String): Int {
        getValidTicket(vehicleNo, LocalDateTime.now(), LocalDateTime.now())?.let { it ->
            return DateUtil.diffDays(LocalDateTime.now(), it.expireDate!!)
        }
        return -1
    }

    fun extendSeasonTicket() {
        // 1. 연장 대상 fetch

        // 연장 대상 상품 리스트
        ticketClassService.findByExtendYn(YN.Y)?.let { ticketClasses ->
            ticketClasses.forEach { ticketClassDTO ->
                // 조건 ( 연장 대상, 달 - 만료 7일 이전 대상 / 일 - period 날짜 전으로 세팅)
                val date =
                ticketClassDTO.period?.let { period ->
                    val number = period["number"] as Int
                    when(period["type"]) {
                        "MONTH" -> { DateUtil.getMinusDays(LocalDateTime.now(), 7) }
                        else -> { DateUtil.getMinusDays(LocalDateTime.now(), min(number.toLong(), 7)) }
                    }
                }?: kotlin.run { LocalDateTime.now() }

                seasonTicketRepository.findByTicketSnAndDelYnAndExtendYnAndEffectDateLessThanAndExpireDateGreaterThanAndTicketTypeAndNextSnIsNullAndPayMethodIsNotNull(ticketClassDTO.sn?: 0, YN.N, YN.Y, date, date, TicketType.SEASONTICKET)?.let { tickets ->
                    tickets.forEach { seasonTicket ->
                        val new = seasonTicketMapper.toDTO(seasonTicket)

                        val expire = seasonTicket.ticket?.period?.let { period ->
                            val number = period["number"] as Int

                            when(period["type"]) {
                                "MONTH" -> {
                                    DateUtil.getAddMonths(seasonTicket.expireDate?: LocalDateTime.now(),
                                        number.toLong()
                                    )
                                }
                                else -> {
                                    DateUtil.getAddDays(seasonTicket.expireDate?: LocalDateTime.now(),
                                        number.toLong()
                                    )
                                }
                            }
                        }

                        new.apply {
                            sn = null
                            effectDate = DateUtil.beginTimeToLocalDateTime(DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(this.expireDate?: LocalDateTime.now(), 1)))
                            expireDate = expire
                            payMethod = null
                        }
                        save(new).let { newSeasonTicketDTO ->
                            val exist = seasonTicketMapper.toDTO(seasonTicket)

                            exist.apply {
                                nextSn = newSeasonTicketDTO.sn
                            }

                            save(exist)
                        }
                    }
                }
            }
        }
    }

    // ticket 비활성화 batch
    fun expireSeasonTicket() {
        // 1. 대상 fetch
        seasonTicketRepository.findByDelYnAndEffectDateLessThanAndExpireDateLessThan(YN.N, LocalDateTime.now(), LocalDateTime.now())?.let { tickets ->
            tickets.forEach { ticket ->
                val update = seasonTicketMapper.toDTO(ticket)
                update.delYn = YN.Y
                save(update)
            }
        }
    }
}