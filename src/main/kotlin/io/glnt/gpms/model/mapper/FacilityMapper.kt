package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.FacilityDTO
import io.glnt.gpms.model.dto.entity.ParkAlarmSettingDTO
import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.repository.ParkAlarmSetttingRepository
import org.springframework.stereotype.Service

@Service
class FacilityMapper(
    private val gateRepository: GateRepository,
    private val parkAlarmSetttingRepository: ParkAlarmSetttingRepository
) {
    fun toDTO(entity: Facility): FacilityDTO {
        FacilityDTO(entity).apply {
            gateRepository.findByGateId(this.gateId!!).ifPresent {
                this.gateType = it.gateType
                this.relaySvrKey = it.relaySvrKey
            }

            ParkAlarmSettingDTO(parkAlarmSetttingRepository.findTopByOrderBySiteid()!!).let {
                this.checkTime = if (this.category == FacilityCategoryType.BREAKER) it.gateLimitTime else 0
                this.counterResetTime = if (this.category == FacilityCategoryType.BREAKER) it.gateCounterResetTime else 0
            }
            return this
        }
    }

    fun toEntity(dto: FacilityDTO) =
        when (dto) {
            null -> null
            else  -> {
                Facility(
                    sn = dto.sn,
                    category = dto.category,
                    modelid = dto.modelid!!,
                    fname = dto.fname!!,
                    dtFacilitiesId = dto.dtFacilitiesId!!,
                    facilitiesId = dto.facilitiesId,
                    gateId = dto.gateId!!,
                    udpGateid = dto.udpGateid,
                    ip = dto.ip,
                    port = dto.port,
                    sortCount = dto.sortCount,
                    resetPort = dto.resetPort,
                    flagConnect = dto.flagConnect,
                    lprType = dto.lprType,
                    imagePath = dto.imagePath,
                    health = dto.health,
                    healthDate = dto.healthDate,
                    status = dto.status,
                    statusDate = dto.statusDate,
                    delYn = dto.delYn
                )
            }
        }
}