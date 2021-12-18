package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.CorpDTO
import io.glnt.gpms.model.dto.entity.CorpTicketDTO
import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.CorpTicketClassRepository
import io.glnt.gpms.service.CorpTicketClassService
import org.springframework.stereotype.Service

@Service
class CorpTicketMapper(
    private val corpTicketClassRepository: CorpTicketClassRepository,
    private val corpRepository: CorpRepository,
    private val corpTicketClassService: CorpTicketClassService
) {
    fun toDTO(entity: CorpTicketInfo): CorpTicketDTO {
        CorpTicketDTO(entity).apply {
            this.corpTicketClass = corpTicketClassService.findBySn(this.classSn!!)
            corpRepository.findBySn(this.corpSn!!)?.let { corp ->
                this.corp = CorpDTO(corp)
            }
            return this
        }
    }

    fun toEntity(dto: CorpTicketDTO) =
        when(dto) {
            null -> null
            else -> {
                CorpTicketInfo(
                    sn = dto.sn,
                    corpSn = dto.corpSn!!,
                    classSn = dto.classSn!!,
                    totalQuantity = dto.totalQuantity!!,
                    useQuantity = dto.useQuantity!!,
                    orderNum = dto.orderNum,
                    delYn = dto.delYn
                )
            }
        }
}