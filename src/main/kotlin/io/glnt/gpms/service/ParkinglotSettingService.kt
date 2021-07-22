package io.glnt.gpms.service

import io.glnt.gpms.model.entity.GateGroup
import io.glnt.gpms.model.repository.GateGroupRepository
import io.glnt.gpms.model.enums.DelYn
import org.springframework.stereotype.Service

@Service
class ParkinglotSettingService(
    private val gateGroupRepository: GateGroupRepository
) {
    fun getGateGroups(): List<GateGroup> {
        return gateGroupRepository.findByDelYn(DelYn.N)
    }

}