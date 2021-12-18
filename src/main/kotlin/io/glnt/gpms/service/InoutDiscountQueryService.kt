package io.glnt.gpms.service

import io.github.jhipster.service.QueryService
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.criteria.InoutDiscountCriteria
import io.glnt.gpms.model.dto.entity.InoutDiscountDTO
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.mapper.InoutDiscountMapper
import io.glnt.gpms.model.repository.InoutDiscountRepository
import mu.KLogging
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
@Transactional(readOnly = true)
class InoutDiscountQueryService(
    private val inoutDiscountRepository: InoutDiscountRepository,
    private val inoutDiscountMapper: InoutDiscountMapper
): QueryService<InoutDiscount>() {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: InoutDiscountCriteria?): MutableList<InoutDiscountDTO> {
        logger.debug("inout-discount find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return inoutDiscountRepository.findAll(specification).mapTo(mutableListOf(), inoutDiscountMapper::toDTO)
    }

    private fun createSpecification(criteria: InoutDiscountCriteria?): Specification<InoutDiscount> {
        val spec = Specification<InoutDiscount> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()
            if (criteria != null) {
                if (criteria.sn != null) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("sn")), criteria.sn)
                    )
                }

                if (criteria.corpSn != null) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("corpSn")), criteria.corpSn)
                    )
                }

                if (criteria.ticketClassSn != null) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("ticketClassSn")), criteria.ticketClassSn)
                    )
                }

                if (criteria.fromDate != null && criteria.toDate != null) {
                    clues.add(
                        criteriaBuilder.between(
                            root.get("createDate"),
                            DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()),
                            DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString())
                        )
                    )
                }
            }
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("createDate")))
            criteriaBuilder.and(*clues.toTypedArray())

        }
        return spec
    }

}