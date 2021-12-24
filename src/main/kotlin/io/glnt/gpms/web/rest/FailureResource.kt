package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.criteria.FailureCriteria
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.service.FailureQueryService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class FailureResource(
    private val failureQueryService: FailureQueryService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/failures"], method = [RequestMethod.GET])
    fun getErrors(@RequestParam(name = "fromDate", required = false) fromDate: String,
                  @RequestParam(name = "toDate", required = false) toDate: String,
                  @RequestParam(name = "category", required = false) category: String,
                  @RequestParam(name = "resolved", required = false) resolved: String): ResponseEntity<CommonResult> {
        val criteria = FailureCriteria(fromDate = DateUtil.stringToLocalDate(fromDate), toDate = DateUtil.stringToLocalDate(toDate), resolved = resolved, category = category )
        var list = failureQueryService.findByCriteria(criteria)
        if (category.isNotEmpty()) {
            return CommonResult.returnResult(CommonResult.data(list.filter { it.category.toString() == category }))
        }
        return CommonResult.returnResult(CommonResult.data(failureQueryService.findByCriteria(criteria)))
    }
}