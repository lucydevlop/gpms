package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.service.DashboardAdminService
import io.glnt.gpms.model.enums.FacilityCategoryType
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/home"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class HomeResource(
    private val dashboardAdminService: DashboardAdminService
) {
    companion object : KLogging()

    @RequestMapping(method = [RequestMethod.GET])
    fun getMainGates() : ResponseEntity<CommonResult> {
        logger.trace { "admin dashboard Gates info" }
        return CommonResult.returnResult(dashboardAdminService.getMainGates())
    }

    @RequestMapping(value = ["/breaker/{gateId}/{action}"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun gateAction(@PathVariable action: String, @PathVariable gateId: String) : ResponseEntity<CommonResult> {
        logger.trace { "gateAction $gateId $action" }
        return CommonResult.returnResult(dashboardAdminService.gateAction(action, gateId))
    }

    @RequestMapping(value = ["/reset/{gateId}/{category}"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun gateResetAction(@PathVariable gateId: String, @PathVariable category: FacilityCategoryType) : ResponseEntity<CommonResult> {
        logger.trace { "gateResetAction $gateId $category" }
        return CommonResult.returnResult(dashboardAdminService.gateResetAction(gateId, category))
    }


}