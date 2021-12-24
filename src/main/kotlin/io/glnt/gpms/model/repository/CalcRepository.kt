package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CgBasic
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.VehicleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FarePolicyRepository: JpaRepository<FarePolicy, Long> {
    fun findByFareNameAndVehicleTypeAndDelYn(fareName: String, vehicleType: VehicleType, delYn: YN): List<FarePolicy>?
    fun findByDelYn(delYn: YN): List<FarePolicy>?
    fun findBySn(sn: Long): FarePolicy?
}

@Repository
interface FareInfoRepository: JpaRepository<FareInfo, Long> {
    fun findBySn(sn: Long): FareInfo
    fun findByFareNameAndDelYn(fareName: String, delYn: YN): FareInfo?
    fun findByDelYn(delYn: YN): List<FareInfo>?
}

@Repository
interface CgBasicRepository: JpaRepository<CgBasic, Long> {
    fun findByDelYn(delYn: YN): CgBasic?
}