package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.TicketClassDTO
import io.glnt.gpms.model.entity.TicketClass
import org.springframework.stereotype.Service

@Service
class TicketClassMapper() {
    fun toDto(entity: TicketClass): TicketClassDTO {
        return TicketClassDTO(entity)
    }

    fun toEntity(dto: TicketClassDTO) =
        when (dto) {
            null -> null
            else -> {
                TicketClass(
                    sn = dto.sn,
                    ticketName = dto.ticketName!!,
                    ticketType = dto.ticketType,
                    aplyType = dto.aplyType,
                    startTime = dto.startTime,
                    endTime = dto.endTime,
                    rangeType = dto.rangeType,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    price = dto.price,
                    vehicleType = dto.vehicleType,
                    available = dto.available,
                    delYn = dto.delYn
                )


            }
        }
}