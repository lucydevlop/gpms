package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.GateDTO
import io.glnt.gpms.model.entity.Gate
import org.springframework.stereotype.Service

@Service
class GateMapper {
    fun toDto(entity: Gate): GateDTO {
        return GateDTO(entity)
    }

    fun toEntity(dto: GateDTO) =
        when (dto) {
            null -> null
            else -> {
                Gate(
                    sn = dto.sn,
                    delYn = dto.delYn,
                    gateName = dto.gateName,
                    gateId = dto.gateId!!,
                    gateType = dto.gateType!!,
                    takeAction = dto.takeAction,
                    udpGateId = dto.udpGateId,
                    openAction = dto.openAction,
                    relaySvrKey = dto.relaySvrKey,
                    relaySvr = dto.relaySvr,
                    resetSvr = dto.resetSvr,
                    gateGroupId = dto.gateGroupId,
                    openType = dto.openType
                )
            }
        }
}