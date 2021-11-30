package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.repository.FailureRepository
import io.glnt.gpms.model.criteria.FailureCriteria
import io.glnt.gpms.model.dto.FailureDTO
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.mapper.FailureMapper
import mu.KLogging
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
@Transactional(readOnly = true)
class FailureQueryService(
    private val failureRepository: FailureRepository,
    private val failureMapper: FailureMapper

) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: FailureCriteria?): MutableList<FailureDTO> {
        logger.debug("failure find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return failureRepository.findAll(specification).mapTo(mutableListOf(), failureMapper::toDTO)
    }

    private fun createSpecification(criteria: FailureCriteria?): Specification<Failure> {
        val spec = Specification<Failure> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()
            if (criteria != null) {
                if (criteria.sn != null) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("sn")), criteria.sn)
                    )
                }
                if (criteria.resolved != null) {
                    if (criteria.resolved.equals("Y")) {
                        clues.add(
                            criteriaBuilder.isNotNull((root.get<LocalDateTime>("expireDateTime")))
                        )
                    } else if (criteria.resolved.equals("N")) {
                            clues.add(
                                criteriaBuilder.isNull((root.get<LocalDateTime>("expireDateTime")))
                            )
                    }
                }

                if (criteria.fromDate != null && criteria.toDate != null) {
                    clues.add(
                        criteriaBuilder.between(
                            root.get("issueDateTime"),
                            DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()),
                            DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString())
                        )
                    )
                }
            }
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("issueDateTime")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }
}