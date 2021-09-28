package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.model.criteria.CorpCriteria
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.service.CorpQueryService
import io.glnt.gpms.service.TicketClassService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@RestController
@RequestMapping(
    path = ["/$API_VERSION/rcs"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class RcsResource(
    private var rcsService: RcsService,
    private val facilityService: FacilityService,
    private val ticketClassService: TicketClassService,
    private val corpQueryService: CorpQueryService
) {

    @RequestMapping(value=["/check/alive"], method = [RequestMethod.GET])
    fun isAlive() : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data("success"))
    }

    @RequestMapping(value=["/async/facilities"], method = [RequestMethod.GET])
    fun asyncFacilities() : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(facilityService.allFacilities()))
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

    // 주차요금 계산
    @RequestMapping(value=["/calc/inout"], method = [RequestMethod.POST])
    fun calcInout(@RequestBody request: resParkInList) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.calcInout(request))
    }

    // 입출차 정보 갱신
    @RequestMapping(value=["/inout"], method = [RequestMethod.PATCH])
    fun updateInout(@RequestBody request: resParkInList) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.updateInout(request))
    }

    @RequestMapping(value=["/tickets"], method = [RequestMethod.GET])
    fun getTickets(@RequestParam(name = "startDate", required = false) startDate: String,
                   @RequestParam(name = "endDate", required = false) endDate: String,
                   @RequestParam(name = "searchDateLabel", required = false) searchDateLabel: DateType,
                   @RequestParam(name = "vehicleNo", required = false) vehicleNo: String
    ) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(
            rcsService.getTickets(
                reqSearchProductTicket(
                    searchDateLabel = searchDateLabel,
                    fromDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    toDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    searchLabel = "CARNUM",
                    searchText = vehicleNo )
        ))
    }

    @RequestMapping(value=["/ticket"], method = [RequestMethod.POST])
    fun createTicket(@RequestBody request: reqCreateProductTicket) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.createTicket(request))
    }

    @RequestMapping(value=["/ticket/classes"], method = [RequestMethod.GET])
    fun getTicketClass() : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(ticketClassService.findAll()))
    }

    @RequestMapping(value=["/discount-classes"], method = [RequestMethod.GET])
    fun getDiscountClasses(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.getDiscountClasses())
    }

    @RequestMapping(value=["/corp/{corpId}"], method = [RequestMethod.GET])
    fun getCorpInfo(@PathVariable corpId: String): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.getCorpInfo(corpId))
    }

    @RequestMapping(value=["/corps"], method = [RequestMethod.GET])
    fun getCorpInfo(): ResponseEntity<CommonResult> {
        val criteria = CorpCriteria(delYn = DelYn.N)
        return CommonResult.returnResult(CommonResult.data(corpQueryService.findByCriteria(criteria)))
    }


}