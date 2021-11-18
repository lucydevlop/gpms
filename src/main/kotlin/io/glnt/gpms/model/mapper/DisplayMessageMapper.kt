package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.DisplayColorDTO
import io.glnt.gpms.model.dto.DisplayMessageDTO
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.repository.DisplayColorRepository
import org.springframework.stereotype.Service

@Service
class DisplayMessageMapper(
    private val displayColorRepository: DisplayColorRepository
) {
    fun toDTO(entity: DisplayMessage): DisplayMessageDTO {
        DisplayMessageDTO(entity).apply {
            displayColorRepository.findByColorCode(this.colorCode!!)?.let {
                this.displayColor = DisplayColorDTO(it)
            }
            return this
        }
    }

    fun toEntity(dto: DisplayMessageDTO) =
        when (dto) {
            null -> null
            else -> {
                DisplayMessage(
                    sn = dto.sn,
                    messageClass = dto.messageClass,
                    messageType = dto.messageType!!,
                    messageCode = dto.messageCode!!,
                    order = dto.order,
                    lineNumber = dto.lineNumber,
                    colorCode = dto.colorCode!!,
                    messageDesc = dto.messageDesc!!,
                    delYn = dto.delYn
                )
            }
        }
}