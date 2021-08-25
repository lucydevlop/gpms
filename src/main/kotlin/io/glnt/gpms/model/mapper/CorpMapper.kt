package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.model.entity.Corp
import org.springframework.stereotype.Service

@Service
class CorpMapper {
    fun toDTO(entity: Corp): CorpDTO {
        return CorpDTO(entity)
    }

    fun toEntity(dto: CorpDTO) =
        when(dto) {
            null -> null
            else  -> {
                Corp(
                    sn = dto.sn,
                    corpId = dto.corpId,
                    corpName = dto.corpName!!,
                    form = dto.form,
                    resident = dto.resident,
                    dong = dto.dong,
                    ho = dto.ho,
                    ceoName = dto.ceoName,
                    tel = dto.tel,
                    mobile = dto.mobile,
                    email = dto.email,
                    address = dto.address,
                    saupNo = dto.saupNo,
                    mobileNo = dto.mobileNo,
                    balance = dto.balance,
                    lastCharging = dto.lastCharging,
                    lastDiscount = dto.lastDiscount,
                    balanceUpdate = dto.balanceUpdate,
                    delYn = dto.delYn
                )
            }
        }
}