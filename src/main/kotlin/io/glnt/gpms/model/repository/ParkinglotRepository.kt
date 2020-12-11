package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.entity.ParkFeature
import io.glnt.gpms.model.entity.ParkSiteInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkSiteInfoRepository: JpaRepository<ParkSiteInfo, String> {
    fun findTopByOrderBySiteid() : ParkSiteInfo?
}

@Repository
interface ParkFeatureRepository: JpaRepository<ParkFeature, Long> {
    fun findByGroupKey(groupKey: String): List<ParkFeature>?
    fun findByFeatureId(featureId: String): ParkFeature?

}

@Repository
interface ParkFacilityRepository: JpaRepository<Facility, Long> {
    fun findByFacilitiesId(facilitiesId: String): Facility?
    fun findByGateSvrKey(gateSvrKey: String): List<Facility>?
}