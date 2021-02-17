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
interface HolidayRepository: JpaRepository<Holiday, Long> {
    fun findByHolidateAndDelYn(date: LocalDate, delYn: DelYn): Holiday?
}

@Repository
interface FarePolicyRepository: JpaRepository<FarePolicy, Long> {
    fun findByFareNameAndVehicleTypeAndDelYn(fareName: String, vehicleType: VehicleType, delYn: DelYn): List<FarePolicy>?
}

@Repository
interface FareInfoRepository: JpaRepository<FareInfo, Long> {
    fun findByFareNameAndDelYn(fareName: String, delYn: DelYn): FareInfo?
}

@Repository
interface CgBasicRepository: JpaRepository<CgBasic, Long> {
    fun findByDelYn(delYn: DelYn): CgBasic?
}