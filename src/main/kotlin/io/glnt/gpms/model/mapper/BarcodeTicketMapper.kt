package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.entity.BarcodeTickets
import org.mapstruct.Mapper
import org.mapstruct.ReportingPolicy

@Mapper(
    componentModel = "spring",
    uses = [],
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
interface BarcodeTicketMapper: EntityMapper<BarcodeTicketsDTO, BarcodeTickets> {
    override fun toDto(entity: BarcodeTickets): BarcodeTicketsDTO
    override fun toEntity(dto: BarcodeTicketsDTO): BarcodeTickets
}