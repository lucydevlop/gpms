package io.glnt.gpms.handler.rcs.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.DisplayMessageClass
import org.apache.poi.ss.usermodel.DateUtil
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping(
    path = ["/$API_VERSION/rcs"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class RcsController(
    private var rcsService: RcsService
) {

    @RequestMapping(value=["/check/alive"], method = [RequestMethod.GET])
    fun isAlive() : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data("success"))
    }

    @RequestMapping(value=["/async/facilities"], method = [RequestMethod.GET])
    fun asyncFacilities() : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.asyncFacilities())
    }

    @RequestMapping(value=["/{facilityId}/{status}"], method = [RequestMethod.GET])
    fun facilityAction(@PathVariable facilityId: String, @PathVariable status: String): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.facilityAction(facilityId, status))
    }

    @RequestMapping(value=["/inouts"], method = [RequestMethod.GET])
    fun getInouts(@RequestParam(name = "startDate", required = false) startDate: String,
                  @RequestParam(name = "endDate", required = false) endDate: String,
                  @RequestParam(name = "searchDateLabel", required = false) searchDateLabel: DisplayMessageClass,
                  @RequestParam(name = "vehicleNo", required = false) vehicleNo: String? = null
    ) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(
            rcsService.getInouts(reqSearchParkin(searchDateLabel = searchDateLabel,
                fromDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                toDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                searchLabel = vehicleNo?.let { "CARNUM" },
                searchText = vehicleNo,
            )))
    }

    @RequestMapping(value=["/inout"], method = [RequestMethod.POST])
    fun createInout(@RequestBody request: resParkInList) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.createInout(request))
    }

    @RequestMapping(value=["/tickets"], method = [RequestMethod.GET])
    fun getTickets(@RequestParam(name = "startDate", required = false) startDate: String,
                   @RequestParam(name = "endDate", required = false) endDate: String) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(
            rcsService.getTickets(reqSearchProductTicket(searchDateLabel = DateType.EFFECT,
                fromDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                toDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")))))
    }


}