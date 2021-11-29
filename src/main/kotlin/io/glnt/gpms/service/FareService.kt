package io.glnt.gpms.service

import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.model.dto.CgBasicDTO
import io.glnt.gpms.model.dto.FareInfoDTO
import io.glnt.gpms.model.dto.FarePolicyDTO
import io.glnt.gpms.model.dto.GateDTO
import io.glnt.gpms.model.entity.CgBasic
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.mapper.CgBasicMapper
import io.glnt.gpms.model.mapper.FareInfoMapper
import io.glnt.gpms.model.mapper.FarePolicyMapper
import io.glnt.gpms.model.repository.CgBasicRepository
import io.glnt.gpms.model.repository.FareInfoRepository
import io.glnt.gpms.model.repository.FarePolicyRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FareService(
    private val farePolicyMapper: FarePolicyMapper,
    private val fareInfoMapper: FareInfoMapper,
    private val farePolicyRepository: FarePolicyRepository,
    private val fareInfoRepository: FareInfoRepository,
    private val fareRefService: FareRefService,
    private val fareBasicRepository: CgBasicRepository,
    private val cgBasicMapper: CgBasicMapper
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findFarePolicies(): List<FarePolicyDTO> {
        return farePolicyRepository.findAll().map(farePolicyMapper::toDto)
    }

    fun saveFarePolicy(farePolicyDTO: FarePolicyDTO) : FarePolicyDTO {
        var farePolicy = farePolicyMapper.toEntity(farePolicyDTO)
        farePolicy = farePolicyRepository.save(farePolicy!!)
        fareRefService.init()
        return farePolicyMapper.toDto(farePolicy)
    }

    fun saveFareInfo(fareInfoDTO: FareInfoDTO): FareInfoDTO {
        var fareInfo = fareInfoMapper.toEntity(fareInfoDTO)
        fareInfo = fareInfoRepository.save(fareInfo!!)
        fareRefService.init()
        return fareInfoMapper.toDTO(fareInfo)
    }

    @Transactional(readOnly = true)
    fun findFareBasic(): CgBasicDTO? {
        return fareBasicRepository.findByDelYn(DelYn.N)?.let { cgBasicMapper.toDTO(it) }
    }
}