package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.model.dto.ProductTicketDTO
import io.glnt.gpms.model.dto.TicketClassDTO
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.TicketClassRepository
import org.springframework.stereotype.Service

@Service
class ProductTicketMapper(
    private val corpRepository: CorpRepository,
    private val ticketClassRepository: TicketClassRepository
) {
    fun toDTO(entity: ProductTicket): ProductTicketDTO {
        ProductTicketDTO(entity).apply {
            this.corp = this.corpSn?.let { CorpDTO(corpRepository.findBySn(this.corpSn ?: -1)!!) }
            this.ticketSn?.let {
                ticketClassRepository.findBySn(it).ifPresent { ticketClass ->
                    this.ticket = TicketClassDTO(ticketClass)
                }
            }
            return this
        }
    }

    fun toEntity(dto: ProductTicketDTO) =
        when (dto) {
            null -> null
            else -> {
                ProductTicket(
                    sn = dto.sn,
                    corpSn = dto.corpSn,
                    ticketSn = dto.ticketSn,
                    corpName = dto.corpName,
                    ticketType = dto.ticketType,
                    vehicleNo = dto.vehicleNo ?: "",
                    color = dto.color,
                    vehiclekind = dto.vehiclekind,
                    vehicleType = dto.vehicleType,
                    name = dto.name,
                    tel = dto.tel,
                    etc = dto.etc,
                    etc1 = dto.etc1,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    delYn = dto.delYn
                )
            }
        }
}