package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.entity.ParkFeature
import io.glnt.gpms.model.entity.ParkSiteInfo
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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
    @Query("SELECT v from Facility v where v.gateSvrKey = :gateSvrKey and ( v.category != 'LPR' or (v.category = 'LPR' and v.imagePath is not null))")
    fun findByGateSvrKey(gateSvrKey: String): List<Facility>?
    fun findByGateIdAndFlagUse(gateId: String, flagUse: Int): List<Facility>?
    fun findByGateIdAndCategory(gateId: String, category: String): List<Facility>?
    fun findByCategory(category: String): List<Facility>?
}

@Repository
interface ParkGateRepository: JpaRepository<Gate, Long> {
    fun findByFlagUse(flagUse: Int): List<Gate>
    fun findByGateId(gateId: String): Gate?
    fun findByUdpGateid(udpGateid: String): Gate?

}