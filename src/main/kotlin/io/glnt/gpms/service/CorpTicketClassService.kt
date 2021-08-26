package io.glnt.gpms.service

import io.glnt.gpms.model.dto.CorpTicketClassDTO
import io.glnt.gpms.model.mapper.CorpTicketClassMapper
import io.glnt.gpms.model.repository.CorpTicketClassRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CorpTicketClassService(
    private val corpTicketClassRepository: CorpTicketClassRepository,
    private val corpTicketClassMapper: CorpTicketClassMapper
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<CorpTicketClassDTO> {
        return corpTicketClassRepository.findAll().map(corpTicketClassMapper::toDTO)
    }

    fun findBySn(sn: Long): CorpTicketClassDTO {
        return corpTicketClassMapper.toDTO(corpTicketClassRepository.findBySn(sn))
    }

    fun save(corpTicketClassDTO: CorpTicketClassDTO): CorpTicketClassDTO {
        logger.debug("Request to save CorpTicketClass : $corpTicketClassDTO")
        val corpTicketClass = corpTicketClassMapper.toEntity(corpTicketClassDTO)
        corpTicketClassRepository.save(corpTicketClass!!)

        return corpTicketClassMapper.toDTO(corpTicketClass)
    }

}