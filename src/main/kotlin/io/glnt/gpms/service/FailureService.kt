package io.glnt.gpms.service

import io.glnt.gpms.model.criteria.FailureCriteria
import io.glnt.gpms.model.dto.FailureDTO
import io.glnt.gpms.model.mapper.FailureMapper
import io.glnt.gpms.model.repository.FailureRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FailureService (
    private val failureQueryService: FailureQueryService,
    private val failureRepository: FailureRepository,
    private val failureMapper: FailureMapper
){
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<FailureDTO> {
        return failureRepository.findAll().map(failureMapper::toDTO)
    }

    fun findByCriteria(criteria: FailureCriteria): List<FailureDTO> {
        return failureQueryService.findByCriteria(criteria)
    }


}