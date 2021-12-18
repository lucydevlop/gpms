package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.DisplayColorDTO
import io.glnt.gpms.model.entity.DisplayColor
import org.springframework.stereotype.Service

@Service
class DisplayColorMapper (
) {
    fun toDTO(entity: DisplayColor) : DisplayColorDTO {
        return DisplayColorDTO(entity)
    }

    fun toEntity(dto: DisplayColorDTO) =
        when (dto) {
            null -> null
            else -> {
                DisplayColor(
                    sn = dto.sn,
                    colorCode = dto.colorCode!!,
                    colorDesc = dto.colorDesc
                )
            }
        }
}