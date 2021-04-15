package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.VehicleListSearch
import io.glnt.gpms.model.entity.TmapCommand
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface TmapCommandRepository: JpaRepository<TmapCommand, Long> {

}

@Repository
interface VehicleListSearchRepository: JpaRepository<VehicleListSearch, String> {
    fun findByRequestId(requestId: String): VehicleListSearch?

}

