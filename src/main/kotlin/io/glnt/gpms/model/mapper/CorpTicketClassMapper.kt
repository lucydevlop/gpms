package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.CorpTicketClassDTO
import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.entity.CorpTicketClass
import io.glnt.gpms.model.repository.DiscountClassRepository
import org.springframework.stereotype.Service

@Service
class CorpTicketClassMapper(
    private val discountClassRepository: DiscountClassRepository
) {
    fun toDTO(entity: CorpTicketClass) : CorpTicketClassDTO {
        CorpTicketClassDTO(entity).apply {
            this.discountClass = DiscountClassDTO(discountClassRepository.findBySn(this.discountClassSn!!))
            return this
        }
    }

    fun toEntity(dto: CorpTicketClassDTO) =
        when(dto) {
            null -> null
            else -> {
                CorpTicketClass(
                    sn = dto.sn,
                    name = dto.name,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    discountClassSn = dto.discountClassSn,
                    onceMax = dto.onceMax,
                    dayMax = dto.dayMax,
                    monthMax = dto.monthMax,
                    saleType = dto.saleType,
                    price = dto.price,
                    extendYn = dto.extendYn,
                    delYn = dto.delYn
                )
            }
        }
}