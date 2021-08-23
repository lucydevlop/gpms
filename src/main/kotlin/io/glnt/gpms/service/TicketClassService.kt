package io.glnt.gpms.service

import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.dto.TicketClassDTO
import io.glnt.gpms.model.mapper.TicketClassMapper
import io.glnt.gpms.model.repository.TicketClassRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TicketClassService(
    private val ticketClassRepository: TicketClassRepository,
    private val ticketClassMapper: TicketClassMapper
) {
    @Transactional(readOnly = true)
    fun findAll(): List<TicketClassDTO> {
        return ticketClassRepository.findAll().map(ticketClassMapper::toDto)
    }

    fun save(ticketClassDTO: TicketClassDTO): TicketClassDTO {
        BarcodeTicketService.logger.debug("Request to save TicketClass : $ticketClassDTO")
        val ticketClass = ticketClassMapper.toEntity(ticketClassDTO)
        ticketClassRepository.save(ticketClass!!)

        return ticketClassMapper.toDto(ticketClass)
    }


}