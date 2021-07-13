package io.glnt.gpms.service

import io.glnt.gpms.model.entity.GateGroup
import io.glnt.gpms.model.repository.GateGroupRepository
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.enums.DelYn
import mu.KLogging
import org.springframework.stereotype.Service
import javax.annotation.PostConstruct

@Service
class GateService(
    private val gateGroupRepository: GateGroupRepository,
    private val gateRepository: GateRepository
) {
    companion object : KLogging()

    lateinit var gates: List<Gate>

    @PostConstruct
    fun initalizeData() {
        gateRepository.findAll().let {
            gates = it
        }

        val defaultGateGroups = ArrayList<GateGroup>()
        defaultGateGroups.add(
            GateGroup(gateGroupId = "GATEGROUP1", gateGroupName = "GATEGROUP1", delYn = DelYn.N, id = null)
        )
        defaultGateGroups.add(
            GateGroup(gateGroupId = "GATEGROUP2", gateGroupName = "GATEGROUP2", delYn = DelYn.N, id = null)
        )

        defaultGateGroups.forEach { gateGroup ->
            gateGroupRepository.findByGateGroupId(gateGroup.gateGroupId)?:run {
                gateGroupRepository.save(gateGroup)
            }
        }
    }

    fun getGateGroups(): List<GateGroup> {
        return gateGroupRepository.findByDelYn(DelYn.N)
    }
}