package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.entity.ParkIn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ParkInRepository: JpaRepository<ParkIn, Long> {
    fun findByVehicleNoEndsWithAndOutSn(vehicleNo: String, outSn: Int) : List<ParkIn>?
    fun findByUdpssid(udpssid: String): ParkIn?
}

@Repository
interface ParkOutRepository: JpaRepository<ParkOut, Long> {
    fun findBySn(sn: Long): ParkOut?
    fun findByVehicleNoEndsWith(vehicleNo: String) : List<ParkOut>?

}