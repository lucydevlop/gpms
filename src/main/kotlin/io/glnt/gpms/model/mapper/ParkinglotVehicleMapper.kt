package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.ParkinglotVehicleDTO
import io.glnt.gpms.model.entity.ParkinglotVehicle
import org.springframework.stereotype.Service

@Service
class ParkinglotVehicleMapper {

    fun toDto(entity: ParkinglotVehicle): ParkinglotVehicleDTO {
        return ParkinglotVehicleDTO(entity)
    }

    fun toEntity(dto: ParkinglotVehicleDTO) =
        when(dto) {
            null -> null
            else -> {
                ParkinglotVehicle(
                    id = dto.id,
                    delYn = dto.delYn,
                    vehicleNo = dto.vehicleNo,
                    type = dto.type,
                    uuid = dto.uuid,
                    image = dto.image,
                    memo = dto.memo
                )
            }
        }
}