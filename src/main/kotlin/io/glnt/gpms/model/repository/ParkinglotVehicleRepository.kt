package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ParkinglotVehicle
import org.springframework.data.jpa.repository.JpaRepository

interface ParkinglotVehicleRepository: JpaRepository<ParkinglotVehicle, Long> {
}