package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ParkSiteInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkSiteInfoRepository: JpaRepository<ParkSiteInfo, String> {
    fun findTopByOrderBySiteid() : ParkSiteInfo?
}