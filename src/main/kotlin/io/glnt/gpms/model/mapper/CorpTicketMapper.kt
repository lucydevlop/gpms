package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.model.dto.CorpTicketDTO
import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.DiscountClassRepository
import org.springframework.stereotype.Service

@Service
class CorpTicketMapper(
    private val discountClassRepository: DiscountClassRepository,
    private val corpRepository: CorpRepository
) {
    fun toDTO(entity: CorpTicketInfo): CorpTicketDTO {
        CorpTicketDTO(entity).apply {
            this.discountClass = DiscountClassDTO(discountClassRepository.findBySn(this.discountClassSn!!))
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
                    discountClassSn = dto.discountClassSn!!,
                    totalQuantity = dto.totalQuantity!!,
                    useQuantity = dto.useQuantity!!,
                    orderNum = dto.orderNum,
                    delYn = dto.delYn
                )
            }
        }
}