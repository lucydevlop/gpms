package io.glnt.gpms.service

import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.model.dto.entity.CgBasicDTO
import io.glnt.gpms.model.dto.entity.FareInfoDTO
import io.glnt.gpms.model.dto.entity.FarePolicyDTO
import io.glnt.gpms.model.enums.YN
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

    @Transactional(readOnly = true)
    fun findFareInfo(): List<FareInfoDTO> {
        return fareInfoRepository.findAll()
            .map {
                FareInfoDTO(it)
            }
    }

    fun saveFareInfo(fareInfoDTO: FareInfoDTO): FareInfoDTO {
        var fareInfo = fareInfoMapper.toEntity(fareInfoDTO)
        fareInfo = fareInfoRepository.save(fareInfo!!)
        fareRefService.init()
        return fareInfoMapper.toDTO(fareInfo)
    }

    @Transactional(readOnly = true)
    fun findFareBasic(): CgBasicDTO? {
        return fareBasicRepository.findByDelYn(YN.N)?.let { cgBasicMapper.toDTO(it) }
    }

    fun saveFareBasic(cgBasicDTO: CgBasicDTO) : CgBasicDTO {
        var fareBasic = cgBasicMapper.toEntity(cgBasicDTO)
        fareBasic = fareBasicRepository.save(fareBasic!!)
        fareRefService.init()
        return cgBasicMapper.toDTO(fareBasic)
    }
}