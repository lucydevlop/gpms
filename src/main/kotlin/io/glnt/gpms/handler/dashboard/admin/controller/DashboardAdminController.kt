package io.glnt.gpms.handler.dashboard.admin.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.service.DashboardAdminService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/dashboard/admin"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DashboardAdminController {
    @Autowired
    private lateinit var dashboardAdminService: DashboardAdminService

    @RequestMapping(value=["/main/gate"], method = [RequestMethod.GET])
    fun getMainGates() : ResponseEntity<CommonResult> {
        logger.info { "admin dashboard Gates info" }
        return CommonResult.returnResult(dashboardAdminService.getMainGates())
    }

    @RequestMapping(value = ["/inout/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkInLists(@RequestBody request: reqSearchParkin) : ResponseEntity<CommonResult> {
        logger.info { "getParkInLists $request" }
        return CommonResult.returnResult(dashboardAdminService.getParkInLists(request))
    }
    companion object : KLogging()
}