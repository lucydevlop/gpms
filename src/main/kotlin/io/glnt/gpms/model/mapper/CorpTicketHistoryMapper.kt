package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.CorpTicketDTO
import io.glnt.gpms.model.dto.CorpTicketHistoryDTO
import io.glnt.gpms.model.entity.CorpTicketHistory
import io.glnt.gpms.model.repository.CorpTicketRepository
import io.glnt.gpms.model.repository.TicketClassRepository
import org.springframework.stereotype.Service

@Service
class CorpTicketHistoryMapper(
    private val corpTicketRepository: CorpTicketRepository
) {
    fun toDTO(entity: CorpTicketHistory): CorpTicketHistoryDTO {
        CorpTicketHistoryDTO(entity).apply {
            this.corpTicket = CorpTicketDTO(corpTicketRepository.findBySn(this.ticketSn!!))
            return this
        }
    }

    fun toEntity(dto: CorpTicketHistoryDTO) =
        when(dto) {
            null -> null
            else -> {
                CorpTicketHistory(
                    sn = dto.sn,
                    ticketSn = dto.ticketSn!!,
                    totalQuantity = dto.totalQuantity!!,
                    useQuantity = dto.useQuantity!!,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    delYn = dto.delYn
                )
            }
        }
}