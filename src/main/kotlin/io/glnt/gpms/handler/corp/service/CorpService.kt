package io.glnt.gpms.handler.corp.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.corp.model.reqSearchCorp
import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.repository.CorpRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
class CorpService {
    companion object : KLogging()

    @Autowired
    private lateinit var corpRepository: CorpRepository

    fun getCorp(request: reqSearchCorp): CommonResult {
        logger.info { "getCorp $request" }
        try {
            request.corpId?.let {
                val list = corpRepository.findByCorpId(it)
                return if (list == null) CommonResult.notfound("corp") else CommonResult.data(list)
            } ?: run {
                corpRepository.findAll(findAllCorpSpecification(request)).let {
                    return CommonResult.data(it)
                }
            }
        } catch (e: CustomException) {
            logger.error { "getCorp ${e.message}" }
            return CommonResult.error("getCorp ${e.message}")
        }
    }

    fun deleteCorp(id: Long) : CommonResult {
        logger.info { "delete corp: $id" }
        try {
            corpRepository.findBySn(id)?.let { corp ->
                corp.delYn = DelYn.Y
                return CommonResult.data(corpRepository.save(corp))
            }
        }catch (e: CustomException) {
            logger.error { "deleteCorp error ${e.message}" }
        }
        return CommonResult.error("deleteCorp failed")
    }

    private fun findAllCorpSpecification(request: reqSearchCorp): Specification<Corp> {
        val spec = Specification<Corp> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if (request.searchLabel == "ID" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.upper(root.get<String>("corpId")), likeValue)
                )
            }

            if (request.searchLabel == "NAME" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("corpName")), likeValue)
                )
            }

            if (request.searchLabel == "MOBILE" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("tel")), likeValue)
                )
            }

            if (request.useStatus != "ALL") {
                clues.add(
                    criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), request.useStatus)
                )
            }

            query.orderBy(criteriaBuilder.asc(root.get<String>("corpId")))
            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }
}