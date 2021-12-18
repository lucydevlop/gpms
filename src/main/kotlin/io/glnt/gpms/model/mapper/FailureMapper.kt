package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.FailureDTO
import io.glnt.gpms.model.entity.Failure
import org.springframework.stereotype.Service

@Service
class FailureMapper() {
    fun toDTO(entity: Failure): FailureDTO {
        return FailureDTO(entity)
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
                    syncYn = dto.syncYn
                )
            }
        }
}