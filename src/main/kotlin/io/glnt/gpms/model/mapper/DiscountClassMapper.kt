package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.DiscountClassDTO
import io.glnt.gpms.model.entity.DiscountClass
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
                    discountApplyRate = dto.discountApplyRate,
                    unit = dto.unit,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    rcsUse = dto.rcsUse,
                    orderNo = dto.orderNo,
                    delYn = dto.delYn
                )
            }
        }
    }
