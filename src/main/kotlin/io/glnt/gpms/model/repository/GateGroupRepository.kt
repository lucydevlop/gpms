package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.GateGroup
import io.glnt.gpms.model.enums.YN
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface GateGroupRepository: JpaRepository<GateGroup, Long> {
    fun findByGateGroupId(gateGroupId: String): GateGroup?

    fun findByDelYn(delYn: YN): List<GateGroup>
}