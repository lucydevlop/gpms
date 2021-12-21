package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.enums.DelYn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface GateRepository : JpaRepository<Gate, Long> {
    fun findBySn(sn: Long): Gate?
    fun findByDelYn(delYn: DelYn): List<Gate>
    fun findByGateId(gateId: String): Gate?
    fun findByUdpGateid(udpGateid: String): Gate?
    fun findByRelaySvrKey(relaySvrKey: String): List<Gate>
    fun findByGateGroupId(gateGroupId: String): List<Gate>
}