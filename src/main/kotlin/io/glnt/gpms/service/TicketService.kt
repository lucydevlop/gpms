package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.entity.SeasonTicketDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.enums.Yn
import io.glnt.gpms.model.mapper.SeasonTicketMapper
import io.glnt.gpms.model.repository.SeasonTicketRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.print.attribute.IntegerSyntax

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
        return seasonTicketRepository.findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(vehicleNo, startDate, endDate,DelYn.N)
            ?.map(seasonTicketMapper::toDTO)
            ?: kotlin.run { null }
    }

    fun getTicketByVehicleNoAndTicketTypeAndRangeDate(vehicleNo: String, ticketType: TicketType, startDate: LocalDateTime, endDate: LocalDateTime): List<SeasonTicketDTO>? {
        return seasonTicketRepository.findByVehicleNoAndTicketTypeAndEffectDateBetweenAndDelYn(vehicleNo, ticketType, startDate, endDate, DelYn.N)
            ?.map(seasonTicketMapper::toDTO)
            ?: kotlin.run { null }
    }

    fun getLastTicketByVehicleNoAndTicketType(vehicleNo: String, ticketType: TicketType) : SeasonTicketDTO? {
        return if (ticketType == TicketType.ALL) seasonTicketRepository.findTopByVehicleNoAndDelYnOrderByExpireDateDesc(vehicleNo, DelYn.N)?.let { seasonTicket -> seasonTicketMapper.toDTO(seasonTicket) }
        else seasonTicketRepository.findTopByVehicleNoAndDelYnAndTicketTypeOrderByExpireDateDesc(vehicleNo, DelYn.N, ticketType)?.let { seasonTicket -> seasonTicketMapper.toDTO(seasonTicket) }
    }

    fun extendSeasonTicket() {
        // 1. 연장 대상 fetch
        // 조건 ( 연장 대상, 만료 7일 이전 대상)
        val date = DateUtil.getMinusDays(LocalDateTime.now(), 7)
        // 연장 대상 상품 리스트
        ticketClassService.findByExtendYn(Yn.Y)?.let { ticketClasses ->
            ticketClasses.forEach { ticketClassDTO ->
                seasonTicketRepository.findByTicketSnAndDelYnAndExtendYnAndEffectDateLessThanAndExpireDateGreaterThanAndTicketTypeAndNextSnIsNull(ticketClassDTO.sn?: 0, DelYn.N, Yn.Y, date, date, TicketType.SEASONTICKET)?.let { tickets ->
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
}