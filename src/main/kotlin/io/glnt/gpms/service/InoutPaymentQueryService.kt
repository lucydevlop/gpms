package io.glnt.gpms.service

import io.github.jhipster.service.QueryService
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.criteria.InoutPaymentCriteria
import io.glnt.gpms.model.dto.entity.InoutPaymentDTO
import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.mapper.InoutPaymentMapper
import io.glnt.gpms.model.repository.InoutPaymentRepository
import mu.KLogging
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate


@Service
@Transactional(readOnly = true)
class InoutPaymentQueryService (
    private val inoutPaymentRepository: InoutPaymentRepository,
    private val inoutPaymentMapper: InoutPaymentMapper
): QueryService<InoutPayment>() {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: InoutPaymentCriteria?): MutableList<InoutPaymentDTO> {
        logger.debug("inout-payment find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return inoutPaymentRepository.findAll(specification).mapTo(mutableListOf(), inoutPaymentMapper::toDTO)
    }

    private fun createSpecification(criteria: InoutPaymentCriteria?): Specification<InoutPayment> {
        val spec = Specification<InoutPayment> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if (criteria != null) {
                if (criteria.fromDate != null && criteria.toDate != null) {
                    clues.add(
                        criteriaBuilder.between(
                            root.get("approveDateTime"),
                            DateUtil.formatDateTime(DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()), "yyyyMMddHHmmss"),
                            DateUtil.formatDateTime(DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString()),"yyyyMMddHHmmss")
                        )
                    )
                }
                if (criteria.vehicleNo != null && criteria.vehicleNo!!.isNotEmpty()) {
                    clues.add(
                        criteriaBuilder.like((root.get<String>("vehicleNo")), "%" + criteria.vehicleNo + "%")
                    )
                }
                if (criteria.resultType != null) {
                    clues.add(
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("result")), criteria.resultType)
                    )
                }

                clues.add(
                    criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), YN.N)
                )
            }
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("createDate")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }

}