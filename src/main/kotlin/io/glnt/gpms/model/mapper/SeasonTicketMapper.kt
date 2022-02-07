package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.CorpDTO
import io.glnt.gpms.model.dto.entity.SeasonTicketDTO
import io.glnt.gpms.model.dto.entity.TicketClassDTO
import io.glnt.gpms.model.entity.SeasonTicket
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.model.repository.TicketClassRepository
import org.springframework.stereotype.Service

@Service
class SeasonTicketMapper(
    private val corpRepository: CorpRepository,
    private val ticketClassRepository: TicketClassRepository,
    private val parkInRepository: ParkInRepository
) {
    fun toDTO(entity: SeasonTicket): SeasonTicketDTO {
        SeasonTicketDTO(entity).apply {
            this.corpName = this.corpSn?.let { corpRepository.findBySn(it)?.corpName }
            this.ticketSn?.let {
                ticketClassRepository.findBySn(it).ifPresent { ticketClass ->
                    this.ticket = TicketClassDTO(ticketClass)
                    this.ticketName = ticketClass.ticketName
                }
            }
            this.lastInDate =
                parkInRepository.findTopByVehicleNoAndOutSnGreaterThanAndDelYnOrderByInDateDesc(this.vehicleNo?: "-", 0, YN.N)?.inDate
            return this
        }
    }

    fun toEntity(dto: SeasonTicketDTO) =
        when (dto) {
            null -> null
            else -> {
                SeasonTicket(
                    sn = dto.sn,
                    corpSn = dto.corpSn,
                    ticketSn = dto.ticketSn,
                    corpName = dto.corpName,
                    ticketType = dto.ticketType,
                    vehicleNo = dto.vehicleNo ?: "",
                    color = dto.color,
                    vehiclekind = dto.vehicleKind,
                    vehicleType = dto.vehicleType,
                    name = dto.name,
                    tel = dto.tel,
                    etc = dto.etc,
                    etc1 = dto.etc1,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    delYn = dto.delYn,
                    extendYn = dto.extendYn,
                    payMethod = dto.payMethod,
                    nextSn = dto.nextSn
                )
            }
        }
}