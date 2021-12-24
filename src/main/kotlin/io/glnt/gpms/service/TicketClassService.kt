package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.TicketClassDTO
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.mapper.TicketClassMapper
import io.glnt.gpms.model.repository.TicketClassRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TicketClassService(
    private val ticketClassRepository: TicketClassRepository,
    private val ticketClassMapper: TicketClassMapper
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<TicketClassDTO> {
        return ticketClassRepository.findAll().map(ticketClassMapper::toDto)
    }

    fun save(ticketClassDTO: TicketClassDTO): TicketClassDTO {
        logger.debug("Request to save TicketClass : $ticketClassDTO")
        val ticketClass = ticketClassMapper.toEntity(ticketClassDTO)
        ticketClassRepository.save(ticketClass!!)

        return ticketClassMapper.toDto(ticketClass)
    }

    fun findByExtendYn(extendYn: YN?) : List<TicketClassDTO>? {
        return ticketClassRepository.findByDelYnAndExtendYn(YN.N, extendYn)?.map(ticketClassMapper::toDto)
    }


}