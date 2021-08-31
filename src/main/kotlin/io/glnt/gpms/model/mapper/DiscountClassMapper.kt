package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.repository.DiscountClassRepository
import org.springframework.stereotype.Service

@Service
class DiscountClassMapper {
    fun toDto(entity: DiscountClass): DiscountClassDTO {
        return DiscountClassDTO(entity)
    }

    fun toEntity(dto: DiscountClassDTO) =
        when(dto) {
            null -> null
            else -> {
                DiscountClass(
                    sn = dto.sn,
                    discountType = dto.discountType,
                    discountNm = dto.discountNm!!,
                    discountApplyType = dto.discountApplyType,
                    unitTime = dto.unitTime,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    delYn = dto.delYn
                )
            }
        }
    }
