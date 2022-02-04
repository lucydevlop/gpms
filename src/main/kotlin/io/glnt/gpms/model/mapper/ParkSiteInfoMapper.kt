package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.ParkSiteInfoDTO
import io.glnt.gpms.model.entity.ParkSiteInfo
import org.springframework.stereotype.Service

@Service
class ParkSiteInfoMapper {
    fun toDTO(entity: ParkSiteInfo): ParkSiteInfoDTO {
        return ParkSiteInfoDTO(entity)
    }

    fun toEntity(dto: ParkSiteInfoDTO) =
        when(dto) {
            null -> null
            else -> {
                ParkSiteInfo(
                    siteId = dto.siteId ?: "",
                    siteName = dto.siteName?: "",
                    limitqty = dto.limitqty,
                    saupNo = dto.saupNo,
                    tel = dto.tel,
                    ceoName = dto.ceoName,
                    postCode = dto.postCode,
                    city = dto.city,
                    address = dto.address,
                    firsttime = dto.firsttime,
                    firstfee = dto.firstfee,
                    returntime = dto.returntime,
                    overtime = dto.overtime,
                    overfee = dto.overfee,
                    addtime = dto.addtime,
                    dayfee = dto.dayfee,
                    parkingSpotStatusNotiCycle = dto.parkingSpotStatusNotiCycle,
                    facilitiesStatusNotiCycle = dto.facilitiesStatusNotiCycle,
                    flagMessage = dto.flagMessage,
                    businame = dto.businame,
                    parkId = dto.parkId,
                    space = dto.space,
                    saleType = dto.saleType,
                    tmapSend = dto.tmapSend,
                    externalSvr = dto.externalSvr,
                    rcsParkId = dto.rcsParkId,
                    ip = dto.ip,
                    vehicleDayOption = dto.vehicleDayOption,
                    visitorExternal = dto.visitorExternal,
                    visitorExternalKey = dto.visitorExternalKey,
                    operatingDays = dto.operatingDays,
                    visitorRegister = dto.visitorRegister,
                    enterNoti = dto.enterNoti,
                    discCriteria = dto.discCriteria
                )
            }
        }
}