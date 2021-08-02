package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.BarcodeClassDTO
import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.entity.BarcodeClass
import io.glnt.gpms.model.repository.DiscountClassRepository
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.springframework.stereotype.Service

@Service
class BarcodeClassMapper (
    private val discountClassRepository: DiscountClassRepository
){
    fun toDto(entity: BarcodeClass): BarcodeClassDTO {
        BarcodeClassDTO(entity).apply {
          this.discountClass = DiscountClassDTO(discountClassRepository.findBySn(this.discountClassSn))
          return this
        }
    }

    fun toEntity(dto: BarcodeClassDTO) =
        when(dto) {
            null -> null
            else -> {
                BarcodeClass(
                    sn = dto.sn,
                    delYn = dto.delYn,
                    start = dto.start,
                    end = dto.end,
                    discountClassSn = dto.discountClassSn
                )
            }
        }
}