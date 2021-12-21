package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.SettingDTO
import io.glnt.gpms.model.entity.Setting
import org.springframework.stereotype.Service

@Service
class SettingMapper {
    fun toDTO(entity: Setting): SettingDTO {
        return SettingDTO(entity)
    }

    fun toEntity(dto: SettingDTO?) =
        when(dto) {
            null -> null
            else -> {
                Setting(
                    sn = dto.sn,
                    code = dto.code,
                    value = dto.value,
                    description = dto.description,
                    delYn = dto.delYn
                )
            }
        }
}