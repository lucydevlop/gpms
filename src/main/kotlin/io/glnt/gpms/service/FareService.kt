package io.glnt.gpms.service

import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.model.dto.FareInfoDTO
import io.glnt.gpms.model.dto.FarePolicyDTO
import io.glnt.gpms.model.mapper.FareInfoMapper
import io.glnt.gpms.model.mapper.FarePolicyMapper
import io.glnt.gpms.model.repository.FareInfoRepository
import io.glnt.gpms.model.repository.FarePolicyRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class FareService(
    private val farePolicyMapper: FarePolicyMapper,
    private val fareInfoMapper: FareInfoMapper,
    private val farePolicyRepository: FarePolicyRepository,
    private val fareInfoRepository: FareInfoRepository,
    private val fareRefService: FareRefService
) {
    companion object : KLogging()

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
}