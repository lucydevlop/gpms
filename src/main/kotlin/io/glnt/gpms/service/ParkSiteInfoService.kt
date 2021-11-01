package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.DiscountApplyDTO
import io.glnt.gpms.model.dto.EnterNotiDTO
import io.glnt.gpms.model.dto.ParkSiteInfoDTO
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.ParkSiteInfoMapper
import io.glnt.gpms.model.repository.ParkSiteInfoRepository
import mu.KLogging
import okhttp3.internal.format
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.annotation.PostConstruct

@Service
class ParkSiteInfoService (
    private val parkSiteInfoRepository: ParkSiteInfoRepository,
    private val parkSiteInfoMapper: ParkSiteInfoMapper
) {
    companion object : KLogging()

    var parkSite: ParkSiteInfoDTO? = null

    @Value("\${visitor-external.url}")
    var visitorExternalUrl: String? = null

    @Value("\${visitor-external.token}")
    var visitorExternalToken: String? = null

    @PostConstruct
    fun initalizeData() {
        val parkSites = parkSiteInfoRepository.findAll().map(parkSiteInfoMapper::toDTO)
        if (parkSites.isNotEmpty()) this.parkSite = parkSites[0] else null

    }

    @Transactional(readOnly = true)
    fun find(): ParkSiteInfoDTO? {
        val parkSites = parkSiteInfoRepository.findAll().map(parkSiteInfoMapper::toDTO)
        return if (parkSites.isNotEmpty()) parkSites[0] else null
    }

    fun save(parkSiteInfoDTO: ParkSiteInfoDTO) : ParkSiteInfoDTO {
        logger.debug("Request to save ParkSiteInfo : $parkSiteInfoDTO")
        val parkSiteInfo = parkSiteInfoMapper.toEntity(parkSiteInfoDTO)
        parkSiteInfoRepository.save(parkSiteInfo!!)
        parkSite = parkSiteInfoMapper.toDTO(parkSiteInfo)
        return parkSiteInfoMapper.toDTO(parkSiteInfo)
    }

    fun delete(parkSiteInfoDTO: ParkSiteInfoDTO) {
        logger.debug("Request to delete ParkSiteInfo : $parkSiteInfoDTO")
        val parkSiteInfo = parkSiteInfoMapper.toEntity(parkSiteInfoDTO)
        return parkSiteInfoRepository.delete(parkSiteInfo!!)
    }

    fun generateRequestId() : String {
        return parkSite?.parkId?.let { DataCheckUtil.generateRequestId(it) } ?: kotlin.run { DataCheckUtil.generateRequestId("") }
    }

    fun getSiteId(): String? {
        return parkSite?.siteId
    }

    fun getParkSiteId(): String? {
        return parkSite?.parkId
    }

    fun getIp(): String? {
        return parkSite?.ip
    }

    fun isTmapSend(): Boolean {
        return parkSite?.let { it.externalSvr == ExternalSvrType.TMAP } ?: kotlin.run { false }
    }

    fun isExternalSend() : Boolean {
        return parkSite?.let { it.externalSvr != ExternalSvrType.NONE && it.externalSvr != null } ?: kotlin.run { false }
    }

    fun isVisitorExternalKeyType(): Boolean {
        return parkSite?.let { it.visitorExternal == VisitorExternalKeyType.APTNER } ?: kotlin.run { false }
    }

    fun isVisitorRegister(): Boolean {
        return parkSite?.let { it.visitorRegister == OnOff.ON } ?: kotlin.run { false }
    }

    fun getVisitorExternalInfo(): HashMap<String, String?>? {
        return parkSite?.visitorExternal?.let {
            hashMapOf<String, String?>(
                "url" to visitorExternalUrl,
                "token" to visitorExternalToken,
                "key" to parkSite!!.visitorExternalKey
            )
        }?: kotlin.run {
            null
        }
    }

    fun checkOperationDay(date: LocalDateTime) : Boolean {
        return this.parkSite?.let { parkSite ->
            // 무휴
            if (parkSite.operatingDays == DiscountRangeType.ALL) return true

            DateUtil.getWeek(DateUtil.LocalDateTimeToDateString(date))?.let { it ->
                val result = when (parkSite.operatingDays) {
                    DiscountRangeType.WEEKDAY -> {
                        it != WeekType.SAT && it != WeekType.SUN
                    }
                    DiscountRangeType.WEEKEND -> {
                        it == WeekType.SAT || it == WeekType.SUN
                    }
                    else -> false
                }
                return result
            }
        }?: kotlin.run {  false }
    }

    fun getEnterNoti(): EnterNotiDTO? {
        return this.parkSite?.enterNoti ?: kotlin.run { null }
    }

    fun getDiscountApply(): DiscountApplyDTO? {
        return this.parkSite?.discApply ?: kotlin.run { null }
    }
}