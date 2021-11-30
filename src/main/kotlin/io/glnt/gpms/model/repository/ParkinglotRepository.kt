package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.enums.LprTypeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ParkSiteInfoRepository: JpaRepository<ParkSiteInfo, String> {
    fun findTopByOrderBySiteId() : ParkSiteInfo?
    fun findBySiteId(siteid: String) : ParkSiteInfo?
}

@Repository
interface ParkAlarmSetttingRepository: JpaRepository<ParkAlarmSetting, String> {
    fun findBySiteid(siteid: String): ParkAlarmSetting?
    fun findTopByOrderBySiteid(): ParkAlarmSetting?
}

@Repository
interface ParkFacilityRepository: JpaRepository<Facility, Long> {
    fun findByFacilitiesId(facilitiesId: String): Facility?
    fun findByDtFacilitiesId(dtFacilitiesId: String): Facility?
//    @Query("SELECT v from Facility v where v.category != 'LPR' or (v.category = 'LPR' and v.imagePath is not null)")
//    fun findByGateSvrKey(gateSvrKey: String): List<Facility>?
    fun findByGateIdAndDelYn(gateId: String, delYn: DelYn): List<Facility>?
    fun findByGateIdAndCategory(gateId: String, category: FacilityCategoryType): List<Facility>?
    fun findByGateIdAndCategoryAndDelYn(gateId: String, category: FacilityCategoryType, delYn: DelYn): List<Facility>?
    fun findByCategory(category: FacilityCategoryType): List<Facility>?
    fun findByGateId(gateId: String): List<Facility>?
    fun findTopByGateIdAndCategoryOrderByStatusDate(gateId: String, category: FacilityCategoryType): Facility
    fun findByGateIdAndCategoryAndDelYnAndLprType(gateId: String, category: FacilityCategoryType, delYn: DelYn, lprTypeStatus: LprTypeStatus) : List<Facility>?
}

@Repository
interface ParkGateRepository: JpaRepository<Gate, Long> {
    fun findBySn(sn: Long): Gate?
    fun findByDelYn(delYn: DelYn): List<Gate>
    fun findByGateId(gateId: String): Gate?
    fun findByUdpGateid(udpGateid: String): Gate?
    fun findByRelaySvrKey(relaySvrKey: String): List<Gate>

}

//@Repository
//interface FailureRepository: JpaRepository<Failure, Long> {
//    fun findTopByFacilitiesIdAndFailureCodeOrderByIssueDateTimeDesc(facilityId: String, failureCode: String): Failure?
//    fun countByFacilitiesIdAndExpireDateTimeIsNull(facilityId: String): Long
//    fun findByFacilitiesIdAndExpireDateTimeIsNull(facilityId: String): Optional<Failure>
//}