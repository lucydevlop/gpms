package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.FacilityDTO
import io.glnt.gpms.model.dto.entity.FailureDTO
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.repository.ParkFacilityRepository
import org.springframework.stereotype.Service

@Service
class FailureMapper(
    private val gateRepository: GateRepository,
    private val facilityRepository: ParkFacilityRepository
) {
    fun toDTO(entity: Failure): FailureDTO {
        FailureDTO(entity).apply {
            this.category?: kotlin.run {
                facilityRepository.findByDtFacilitiesId(this.facilitiesId?: "")?.let {
                    this.category = it.category
                    this.gateId?: kotlin.run { this.gateId = it.gateId }
                }
            }

            gateRepository.findByGateId(this.gateId?: "")?.let {
                this.gateName = it.gateName
            }


            return this
        }
    }

    fun toEntity(dto: FailureDTO) =
        when(dto) {
            null -> null
            else -> {
                Failure(
                    sn = dto.sn,
                    issueDateTime = dto.issueDateTime!!,
                    expireDateTime = dto.expireDateTime,
                    facilitiesId = dto.facilitiesId,
                    fName = dto.fName,
                    failureCode = dto.failureCode,
                    failureType = dto.failureType,
                    failureFlag = dto.failureFlag,
                    syncYn = dto.syncYn,
                    category = dto.category,
                    gateId = dto.gateId
                )
            }
        }
}