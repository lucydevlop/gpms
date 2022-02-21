package io.glnt.gpms.service

import io.github.jhipster.service.QueryService
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.criteria.ParkInCriteria
import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
@Transactional(readOnly = true)
class ParkInQueryService(
    private val parkInRepository: ParkInRepository,
    private val parkInMapper: ParkInMapper
): QueryService<ParkIn>() {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ParkInCriteria?): MutableList<ParkInDTO> {
        logger.debug("parkin find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return parkInRepository.findAll(specification).mapTo(mutableListOf(), parkInMapper::toDTO)
    }

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ParkInCriteria?, pageable: Pageable): Page<ParkIn> {
        logger.debug("parkin find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return parkInRepository.findAll(specification, pageable)
    }

    private fun createSpecification(criteria: ParkInCriteria?): Specification<ParkIn> {
        val spec = Specification<ParkIn> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()
            if (criteria != null) {
                if (criteria.sn != null) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("sn")), criteria.sn)
                    )
                }

                if (criteria.uuid != null && criteria.uuid!!.isNotEmpty()) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("uuid")), criteria.uuid)
                    )
                }

                if (criteria.vehicleNo != null && criteria.vehicleNo!!.isNotEmpty()) {
                    clues.add(
                        criteriaBuilder.like((root.get<String>("vehicleNo")), "%" + criteria.vehicleNo + "%")
                    )
                }

                if (criteria.parkcartype != null && criteria.parkcartype!!.isNotEmpty()) {
                    clues.add(
                        criteriaBuilder.like((root.get<String>("parkcartype")), "%" + criteria.parkcartype + "%")
                    )
                }

                if (criteria.fromDate != null && criteria.toDate != null) {
                    clues.add(
                        criteriaBuilder.between(
                            root.get("inDate"),
                            DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()),
                            DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString())
                        )
                    )
                }

                if (criteria.gateId != null && criteria.gateId!!.isNotEmpty()) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("gateId")), criteria.gateId)
                    )
                }

                if (criteria.outSn != null) {
                    clues.add(
                        criteriaBuilder.notEqual((root.get<String>("outSn")), criteria.outSn)
                    )
                }

                if (criteria.delYn != null) {
                    clues.add(
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), criteria.delYn)
                    )
                }
            }
            clues.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), YN.N))
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("inDate")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }


}