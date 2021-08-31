package io.glnt.gpms.service

import io.glnt.gpms.model.dto.ParkSiteInfoDTO
import io.glnt.gpms.model.mapper.ParkSiteInfoMapper
import io.glnt.gpms.model.repository.ParkSiteInfoRepository
import mu.KLogging
import okhttp3.internal.format
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ParkSiteInfoService (
    private val parkSiteInfoRepository: ParkSiteInfoRepository,
    private val parkSiteInfoMapper: ParkSiteInfoMapper
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun find(): ParkSiteInfoDTO {
        return parkSiteInfoRepository.findAll().map(parkSiteInfoMapper::toDTO)[0]
    }

    fun getSiteId(): String? {
        return this.find().siteid
    }

    fun getParkSiteId(): String? {
        return this.find().parkId
    }

    fun getIp(): String? {
        return this.find().ip
    }

    fun save(parkSiteInfoDTO: ParkSiteInfoDTO) : ParkSiteInfoDTO {
        logger.debug("Request to save ParkSiteInfo : $parkSiteInfoDTO")
        val parkSiteInfo = parkSiteInfoMapper.toEntity(parkSiteInfoDTO)
        parkSiteInfoRepository.save(parkSiteInfo!!)
        return parkSiteInfoMapper.toDTO(parkSiteInfo)
    }
}