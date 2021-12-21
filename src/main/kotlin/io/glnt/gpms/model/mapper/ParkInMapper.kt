package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.GateDTO
import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.dto.entity.SeasonTicketDTO
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.repository.SeasonTicketRepository
import org.springframework.stereotype.Service

@Service
class ParkInMapper(
    private val seasonTicketRepository: SeasonTicketRepository,
    private val gateRepository: GateRepository
) {
    fun toDTO(entity: ParkIn) : ParkInDTO {
        ParkInDTO(entity).apply {
            this.seasonTicketDTO = seasonTicketRepository.findBySn(this.ticketSn ?: 0)?.let { SeasonTicketDTO(it) }
            gateRepository.findByGateId(this.gateId!!)?.let {
                this.gate = GateDTO(it)
            }
            return this
        }
    }

    fun toEntity(dto: ParkInDTO) =
        when(dto) {
            null -> null
            else -> {
                ParkIn(
                    sn = dto.sn,
                    gateId = dto.gateId,
                    parkcartype = dto.parkcartype,
                    vehicleNo = dto.vehicleNo,
                    inDate = dto.inDate,
                    date = dto.date,
                    image = dto.image,
                    outSn = dto.outSn,
                    resultcode = dto.resultcode,
                    requestid = dto.requestid,
                    uuid = dto.uuid,
                    ticketSn = dto.ticketSn,
                    memo = dto.memo,
                    delYn = dto.delYn
                )
            }
        }
}