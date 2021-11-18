package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CgBasic
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.entity.Holiday
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.VehicleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface FarePolicyRepository: JpaRepository<FarePolicy, Long> {
    fun findByFareNameAndVehicleTypeAndDelYn(fareName: String, vehicleType: VehicleType, delYn: DelYn): List<FarePolicy>?
    fun findByDelYn(delYn: DelYn): List<FarePolicy>?
    fun findBySn(sn: Long): FarePolicy?
}

@Repository
interface FareInfoRepository: JpaRepository<FareInfo, Long> {
    fun findBySn(sn: Long): FareInfo
    fun findByFareNameAndDelYn(fareName: String, delYn: DelYn): FareInfo?
    fun findByDelYn(delYn: DelYn): List<FareInfo>?
}

@Repository
interface CgBasicRepository: JpaRepository<CgBasic, Long> {
    fun findByDelYn(delYn: DelYn): CgBasic?
}