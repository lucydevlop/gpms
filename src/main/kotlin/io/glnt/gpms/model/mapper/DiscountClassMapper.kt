package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.repository.DiscountClassRepository
import org.springframework.stereotype.Service

@Service
class DiscountClassMapper (){
    fun toDto(entity: DiscountClass): DiscountClassDTO {
        return DiscountClassDTO(entity)
    }
}