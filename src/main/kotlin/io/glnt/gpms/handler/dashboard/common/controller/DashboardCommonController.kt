package io.glnt.gpms.handler.dashboard.common.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.dashboard.common.model.reqParkingDiscountSearchTicket
import io.glnt.gpms.io.glnt.gpms.handler.dashboard.common.service.DashboardCommService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/dashboard/common"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DashboardCommonController {
    @Autowired
    private lateinit var dashboardCommService: DashboardCommService

    @RequestMapping(value=["/parking/discount/search/ticket"], method = [RequestMethod.POST])
    fun parkingDiscountSearchTicket(@RequestBody request: reqParkingDiscountSearchTicket) : ResponseEntity<CommonResult> {
        logger.info { "parkingDiscountSearchTicket $request" }
        return CommonResult.returnResult(dashboardCommService.parkingDiscountSearchTicket(request))
    }

    companion object : KLogging()
}