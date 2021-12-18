package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.criteria.SeasonTicketCriteria
import io.glnt.gpms.model.dto.entity.SeasonTicketDTO
import io.glnt.gpms.model.entity.SeasonTicket
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.mapper.SeasonTicketMapper
import io.glnt.gpms.model.repository.SeasonTicketRepository
import mu.KLogging
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.criteria.Predicate

@Service
@Transactional(readOnly = true)
class SeasonTicketQueryService(
    private val seasonTicketMapper: SeasonTicketMapper,
    private val seasonTicketRepository: SeasonTicketRepository
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findByCriteria(criteria: SeasonTicketCriteria): MutableList<SeasonTicketDTO> {
        logger.trace("seasonTicket find by criteria : {}", criteria)
        val specification = createSpecification(criteria)
        return seasonTicketRepository.findAll(specification).mapTo(mutableListOf(), seasonTicketMapper::toDTO)
    }

    private fun createSpecification(criteria: SeasonTicketCriteria?): Specification<SeasonTicket> {
        val spec = Specification<SeasonTicket> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()
            if (criteria != null) {
                if (criteria.sn != null) {
                    clues.add(
                        criteriaBuilder.equal((root.get<String>("sn")), criteria.sn)
                    )
                }
                if (criteria.searchLabel != null && criteria.searchText != null) {
                    val likeValue = "%" + criteria.searchText + "%"
                    if (criteria.searchLabel == "CARNUM") {
                        clues.add(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                        )
                    } else if (criteria.searchLabel == "USERNAME") {
                        clues.add(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("name")), likeValue)
                        )
                    }
                }

                if (criteria.searchDateLabel != null && criteria.fromDate != null && criteria.toDate != null) {
                    when(criteria.searchDateLabel) {
                        DateType.EFFECT -> {
                            clues.add(
                                criteriaBuilder.between(
                                    root.get("effectDate"),
                                    DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()),
                                    DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString())
                                )
                            )
                        }
                        DateType.EXPIRE -> {
                            clues.add(
                                criteriaBuilder.between(
                                    root.get("expireDate"),
                                    DateUtil.beginTimeToLocalDateTime(criteria.fromDate.toString()),
                                    DateUtil.lastTimeToLocalDateTime(criteria.toDate.toString())
                                )
                            )
                        }
                        else -> {
                            clues.add(
                                criteriaBuilder.lessThanOrEqualTo(
                                    root.get("effectDate"),
                                    DateUtil.beginTimeToLocalDateTime(criteria.toDate.toString())
                                )
                            )
                            clues.add(
                                criteriaBuilder.greaterThanOrEqualTo(
                                    root.get("expireDate"),
                                    DateUtil.lastTimeToLocalDateTime(criteria.fromDate.toString())
                                )
                            )
                        }
                    }
                }
                if (criteria.ticketType != null && criteria.ticketType != TicketType.ALL ) {
                    clues.add(
                        criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("ticketType")), criteria.ticketType)
                    )
                }

                if (criteria.delYn != "ALL") {
                    when(criteria.delYn) {
                        "Y" -> {
                            clues.add(
                                criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.Y)
                            )
                        }
                        else -> {
                            clues.add(
                                criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N)
                            )
                        }
                    }
                }
            }
            //query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("issueDateTime")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }

}