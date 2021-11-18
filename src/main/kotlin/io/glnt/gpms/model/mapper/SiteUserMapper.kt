package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.SiteUserDTO
import io.glnt.gpms.model.entity.SiteUser
import org.springframework.stereotype.Service

@Service
class SiteUserMapper {
    fun toDTO(entity: SiteUser): SiteUserDTO {
        return SiteUserDTO(entity)
    }

    fun toEntity(dto: SiteUserDTO) =
        when (dto) {
            null -> null
            else -> {
                SiteUser(
                    idx = dto.idx,
                    id = dto.id!!,
                    password = dto.password!!,
                    userName = dto.userName!!,
                    userPhone = dto.userPhone,
                    userEmail = dto.userEmail,
                    checkUse = dto.checkUse,
                    wrongCount = dto.wrongCount,
                    passwordDate = dto.passwordDate,
                    role = dto.role,
                    loginDate = dto.loginDate,
                    corpSn = dto.corpSn,
                    delYn = dto.delYn
                )
            }
        }
}