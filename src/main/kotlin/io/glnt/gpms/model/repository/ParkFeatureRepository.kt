package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ParkFeature
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ParkFeatureRepository: JpaRepository<ParkFeature, Long> {
    fun findByGroupKey(groupKey: String): List<ParkFeature>?

}