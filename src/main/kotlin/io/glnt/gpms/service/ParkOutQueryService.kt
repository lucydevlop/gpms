package io.glnt.gpms.service

import io.github.jhipster.service.QueryService
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.ParkOutCriteria
import io.glnt.gpms.model.dto.ParkOutDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.mapper.ParkOutMapper
import io.glnt.gpms.model.repository.ParkOutRepository
import mu.KLogging
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
@Transactional(readOnly = true)
class ParkOutQueryService(
    private val parkOutRepository: ParkOutRepository,
    private val parkOutMapper: ParkOutMapper
) : QueryService<ParkOut>() {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: ParkOutCriteria?): MutableList<ParkOutDTO> {
        logger.debug("parkout find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return parkOutRepository.findAll(specification).mapTo(mutableListOf(), parkOutMapper::toDTO)
    }

    private fun createSpecification(criteria: ParkOutCriteria?): Specification<ParkOut> {
        val spec = Specification<ParkOut> { root, query, criteriaBuilder ->
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
                            root.get("outDate"),
                            DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()),
                            DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString())
                        )
                    )
                }
            }
            clues.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N))
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("outDate")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }


}