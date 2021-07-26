package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.BarcodeDTO
import io.glnt.gpms.model.entity.Barcode
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    uses = [],
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface BarcodeMapper: EntityMapper<BarcodeDTO, Barcode> {
    override fun toDto(entity: Barcode): BarcodeDTO
    override fun toEntity(dto: BarcodeDTO): Barcode
}