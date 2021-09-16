package io.glnt.gpms.service

import io.glnt.gpms.model.mapper.GateMapper
import io.glnt.gpms.model.dto.GateDTO
import io.glnt.gpms.model.entity.GateGroup
import io.glnt.gpms.model.repository.GateGroupRepository
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.enums.DelYn
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*
import javax.annotation.PostConstruct
import kotlin.collections.ArrayList

@Service
class GateService(
    private val gateGroupRepository: GateGroupRepository,
    private val gateRepository: GateRepository,
    private val gateMapper: GateMapper
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

    @Transactional(readOnly = true)
    fun findAll(): List<GateDTO> {
        return gateRepository.findAll().map(gateMapper::toDto)
    }

    fun getGateGroups(): List<GateGroup> {
        return gateGroupRepository.findByDelYn(DelYn.N)
    }

    fun saveGate(gateDTO: GateDTO) : GateDTO {
        var gate = gateMapper.toEntity(gateDTO)
        gate = gateRepository.save(gate!!)
        return gateMapper.toDto(gate)
    }

    fun findOne(gateId: String): Optional<GateDTO> {
        logger.debug { "Request to get Gate $gateId" }
        return gateRepository.findByGateId(gateId).map(gateMapper::toDto)

    }

    fun findActiveGate(): List<GateDTO> {
        logger.debug { "Request to get Active Gate " }
        return gateRepository.findByDelYn(DelYn.N).map(gateMapper::toDto)
    }
}