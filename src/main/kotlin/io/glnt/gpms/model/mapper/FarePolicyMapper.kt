package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.FareInfoDTO
import io.glnt.gpms.model.dto.FarePolicyDTO
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.repository.FareInfoRepository
import org.springframework.stereotype.Service

@Service
class FarePolicyMapper(
    private val fareInfoRepository: FareInfoRepository
) {
    fun toDto(entity: FarePolicy) : FarePolicyDTO {
        FarePolicyDTO(entity).apply {
            this.addFareSn?.let {
                this.addFare = FareInfoDTO(fareInfoRepository.findBySn(it))
            }
            this.basicFareSn?.let {
                this.basicFare = FareInfoDTO(fareInfoRepository.findBySn(it))
            }
            return this
        }
    }

    fun toEntity(dto: FarePolicyDTO) =
        when(dto) {
            null -> null
            else -> FarePolicy(
                sn = dto.sn,
                fareName = dto.fareName!!,
                vehicleType = dto.vehicleType,
                startTime = dto.startTime,
                endTime = dto.endTime,
                basicFareSn = dto.basicFareSn!!,
                addFareSn = dto.addFareSn,
                effectDate = dto.effectDate,
                expireDate = dto.expireDate,
                week = dto.week,
                delYn = dto.delYn
            )
        }
}