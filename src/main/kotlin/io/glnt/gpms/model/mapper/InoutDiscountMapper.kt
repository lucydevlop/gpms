package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.dto.InoutDiscountDTO
import io.glnt.gpms.model.dto.ParkInDTO
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.repository.DiscountClassRepository
import io.glnt.gpms.model.repository.ParkInRepository
import org.springframework.stereotype.Service

@Service
class InoutDiscountMapper(
    private val discountClassRepository: DiscountClassRepository,
    private val parkInRepository: ParkInRepository
) {
    fun toDTO(entity: InoutDiscount): InoutDiscountDTO {
        InoutDiscountDTO(entity).apply {
            this.discountClass = DiscountClassDTO(discountClassRepository.findBySn(this.discountClassSn!!))
            this.parkInDTO = parkInRepository.findBySn(this.inSn!!)?.let { ParkInDTO(it) }
            return this
        }
    }

    fun toEntity(dto: InoutDiscountDTO) =
        when(dto){
            null -> null
            else -> {
                InoutDiscount(
                    sn = dto.sn,
                    discontType = dto.discontType,
                    corpSn = dto.corpSn,
                    discountClassSn = dto.discountClassSn!!,
                    ticketHistSn = dto.ticketHistSn,
                    inSn = dto.inSn!!,
                    quantity = dto.quantity,
                    useQuantity = dto.useQuantity,
                    applyDate = dto.applyDate,
                    calcYn = dto.calcYn,
                    outSn = dto.outSn,
                    delYn = dto.delYn
                )
            }
        }
}