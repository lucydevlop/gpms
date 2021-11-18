package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.HolidayDTO
import io.glnt.gpms.model.entity.Holiday
import org.springframework.stereotype.Service

@Service
class HolidayMapper {
    fun toDto(entity: Holiday) : HolidayDTO {
        return HolidayDTO(entity)
    }

    fun toEntity(dto: HolidayDTO) =
        when(dto) {
            null -> null
            else -> {
                Holiday(
                    sn = dto.sn,
                    name = dto.name,
                    startDate = dto.startDate!!,
                    endDate = dto.endDate!!,
                    startTime = dto.startTime,
                    endTime = dto.endTime,
                    type = dto.type,
                    isWorking = dto.isWorking,
                    delYn = dto.delYn
                )
            }
        }
}