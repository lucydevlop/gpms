package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ParkIn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkInRepository: JpaRepository<ParkIn, Long> {
}