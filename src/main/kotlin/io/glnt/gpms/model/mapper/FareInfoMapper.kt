package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.FareInfoDTO
import io.glnt.gpms.model.entity.FareInfo
import org.springframework.stereotype.Service

@Service
class FareInfoMapper {
    fun toDTO(entity: FareInfo): FareInfoDTO {
        return FareInfoDTO(entity)
    }

    fun toEntity(dto: FareInfoDTO) =
        when (dto) {
            null -> null
            else -> {
                FareInfo(
                    sn = dto.sn,
                    fareName = dto.fareName!!,
                    effectDate = dto.effectDate,
                    expireDate = dto.expireDate,
                    type = dto.type,
                    time1 = dto.time1,
                    won1 = dto.won1,
                    count1 = dto.count1,
                    time2 = dto.time2,
                    won2 = dto.won2,
                    count2 = dto.count2,
                    time3 = dto.time3,
                    won3 = dto.won3,
                    count3 = dto.count3,
                    count = dto.count,
                    delYn = dto.delYn
                )
            }
        }
}