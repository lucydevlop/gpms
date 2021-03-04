package io.glnt.gpms.handler.dashboard.admin.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.model.reqChangeUseGate
import io.glnt.gpms.handler.dashboard.admin.model.reqCreateFacility
import io.glnt.gpms.handler.dashboard.admin.model.reqCreateMessage
import io.glnt.gpms.handler.dashboard.admin.service.DashboardAdminService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import retrofit2.http.Path

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

    @RequestMapping(value = ["/gate/list"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getGates(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(dashboardAdminService.getGates())
    }

    @RequestMapping(value = ["/gate/{action}/{gateId}"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun gateAction(@PathVariable action: String, @PathVariable gateId: String) : ResponseEntity<CommonResult> {
        logger.info { "gateAction $gateId $action" }
        return CommonResult.returnResult(dashboardAdminService.gateAction(action, gateId))
    }

    @RequestMapping(value = ["/gate/change/use"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun changeGateUse(@RequestBody request: reqChangeUseGate): ResponseEntity<CommonResult> {
        logger.info("parkinglot gate use change : $request")
        return CommonResult.returnResult(dashboardAdminService.changeGateUse(request))
    }

    @RequestMapping(value = ["/facility/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createFacility(@RequestBody request: reqCreateFacility) : ResponseEntity<CommonResult> {
        logger.info { "createFacility $request " }
        return CommonResult.returnResult(dashboardAdminService.createFacility(request))
    }

    @RequestMapping(value = ["/message/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createMessage(@RequestBody request: reqCreateMessage) : ResponseEntity<CommonResult> {
        logger.info { "$request" }
        return CommonResult.returnResult(dashboardAdminService.createMessage(request))
    }

    companion object : KLogging()
}