package io.glnt.gpms.service

import io.glnt.gpms.model.dto.SeasonTicketDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.mapper.SeasonTicketMapper
import io.glnt.gpms.model.repository.SeasonTicketRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class TicketService(
    private val seasonTicketMapper: SeasonTicketMapper,
    private val seasonTicketRepository: SeasonTicketRepository,
    private val corpService: CorpService
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
            val ticket = save(productTicketDTO)
            list.add(ticket)
        }
        return list
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

    fun getLastTicketByVehicleNoAndTicketType(vehicleNo: String, ticketType: TicketType) : SeasonTicketDTO? {
        return if (ticketType == TicketType.ALL) seasonTicketRepository.findTopByVehicleNoAndDelYnOrderByExpireDateDesc(vehicleNo, DelYn.N)?.let { seasonTicket -> seasonTicketMapper.toDTO(seasonTicket) }
        else seasonTicketRepository.findTopByVehicleNoAndDelYnAndTicketTypeOrderByExpireDateDesc(vehicleNo, DelYn.N, ticketType)?.let { seasonTicket -> seasonTicketMapper.toDTO(seasonTicket) }
    }
}