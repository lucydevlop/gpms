package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ParkIn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ParkInRepository: JpaRepository<ParkIn, Long> {
    fun findByVehicleNoEndsWithAndOutSn(vehicleNo: String, outSn: Int) : List<ParkIn>?
}