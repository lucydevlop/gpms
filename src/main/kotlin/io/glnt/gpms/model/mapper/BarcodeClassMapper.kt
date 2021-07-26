package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.BarcodeClassDTO
import io.glnt.gpms.model.dto.BarcodeDTO
import io.glnt.gpms.model.entity.Barcode
import io.glnt.gpms.model.entity.BarcodeClass
import io.glnt.gpms.model.mapper.EntityMapper
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    uses = [],
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface BarcodeClassMapper: EntityMapper<BarcodeClassDTO, BarcodeClass> {
    override fun toDto(entity: BarcodeClass): BarcodeClassDTO
    override fun toEntity(dto: BarcodeClassDTO): BarcodeClass
}