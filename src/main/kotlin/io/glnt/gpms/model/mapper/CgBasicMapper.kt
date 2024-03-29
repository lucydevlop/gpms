package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.CgBasicDTO
import io.glnt.gpms.model.entity.CgBasic
import org.springframework.stereotype.Service

@Service
class CgBasicMapper {
    fun toDTO(entity: CgBasic): CgBasicDTO {
        return CgBasicDTO(entity)
    }

    fun toEntity(dto: CgBasicDTO) =
        when(dto) {
            null -> null
            else -> {
                CgBasic(
                    sn = dto.sn,
                    effectDate = dto.effectDate,
                    serviceTime = dto.serviceTime,
                    legTime = dto.legTime,
                    ticketTime = dto.ticketTime,
                    dayMaxAmt = dto.dayMaxAmt,
                    delYn = dto.delYn
                )
            }
        }
}