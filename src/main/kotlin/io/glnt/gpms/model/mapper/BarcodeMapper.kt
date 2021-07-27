package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.BarcodeDTO
import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.entity.Barcode
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.springframework.stereotype.Service

@Service
class BarcodeMapper {
    fun toDto(entity: Barcode): BarcodeDTO {
        return BarcodeDTO(entity)
    }

    fun toEntity(dto: BarcodeDTO) =
        when (dto) {
            null -> null
            else -> {
                Barcode(
                    sn = dto.sn,
                    delYn = dto.delYn,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    startIndex = dto.startIndex,
                    endIndex = dto.endIndex,
                    decriptKey = dto.decriptKey
                )
            }
        }
}