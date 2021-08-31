package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.entity.BarcodeTickets
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy
import org.springframework.stereotype.Service

@Service
class BarcodeTicketsMapper {
    fun toDto(entity: BarcodeTickets): BarcodeTicketsDTO {
        return BarcodeTicketsDTO(entity)
    }

    fun toEntity(dto: BarcodeTicketsDTO) =
        when (dto) {
            null -> null
            else -> {
                BarcodeTickets(
                    sn = dto.sn,
                    barcode = dto.barcode,
                    inSn = dto.inSn,
                    vehicleNo = dto.vehicleNo,
                    price = dto.price,
                    applyDate = dto.applyDate,
                    delYn = dto.delYn
                )
            }
        }

}