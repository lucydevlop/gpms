package io.glnt.gpms.service

import io.github.jhipster.service.QueryService
import io.glnt.gpms.model.dto.CorpCriteria
import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.mapper.CorpMapper
import io.glnt.gpms.model.repository.CorpRepository
import mu.KLogging
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.criteria.Predicate

@Service
@Transactional(readOnly = true)
class CorpQueryService (
    private val corpRepository: CorpRepository,
    private val corpMapper: CorpMapper
): QueryService<Corp>() {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: CorpCriteria?): MutableList<CorpDTO> {
        logger.debug("corp find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return corpRepository.findAll(specification).mapTo(mutableListOf(), corpMapper::toDTO )
    }

    private fun createSpecification(criteria: CorpCriteria?): Specification<Corp> {
//        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        val spec = Specification<Corp> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()
            if (criteria != null) {
                if (criteria.sn != null) {
                    clues.add(
                        criteriaBuilder.equal(criteriaBuilder.lower(root.get<String>("sn")), criteria.sn)
                    )
                }
                if (criteria.corpName != null) {
                    clues.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("corpName")), "%" + criteria.corpName + "%")
                    )
                }
                if (criteria.corpId != null) {
                    clues.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("corpId")), "%" + criteria.corpId + "%")
                    )
                }
                if (criteria.tel != null) {
                    clues.add(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("tel")), "%" + criteria.tel + "%")
                    )
                }
                if (criteria.delYn != null) {
                    clues.add(
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), criteria.delYn)
                    )
                }
            }
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
//        return specification
    }

//    @Nullable
//    fun <T> where(@Nullable spec: Specification<T>?): Specification<T>? {
//        return spec
//            ?: Specification { root: Root<T>?, query: CriteriaQuery<*>?, builder: CriteriaBuilder? -> null }
//    }
}