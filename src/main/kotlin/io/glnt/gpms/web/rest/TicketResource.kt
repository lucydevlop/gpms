package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.model.criteria.SeasonTicketCriteria
import io.glnt.gpms.model.dto.SeasonTicketDTO
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.service.ParkSiteInfoService
import io.glnt.gpms.service.SeasonTicketQueryService
import io.glnt.gpms.service.TicketService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class TicketResource (
    private val ticketService: TicketService,
    private val parkSiteInfoService: ParkSiteInfoService,
    private val authService: AuthService,
    private val seasonTicketQueryService: SeasonTicketQueryService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/tickets/visitors"], method = [RequestMethod.POST])
    fun createVisitors(@Valid @RequestBody ticketDTOs: ArrayList<SeasonTicketDTO>, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        // 방문권 등록 가능 여부 확인
        if (authService.isStoreUser(servlet) && !parkSiteInfoService.isVisitorRegister()) {
            return CommonResult.returnResult(CommonResult.error(ResultCode.CONFLICT.getCode(), "방문권 등록이 불가합니다"))
//            throw CustomException(
//                "방문권 등록이 불가합니다",
//                ResultCode.FAILED
//            )
        }
        return CommonResult.returnResult(CommonResult.data(ticketService.saveTickets(ticketDTOs)))
    }

    @RequestMapping(value = ["/tickets"], method = [RequestMethod.GET])
    fun getTickets(@RequestParam(name = "fromDate", required = false) fromDate: String,
                   @RequestParam(name = "toDate", required = false) toDate: String,
                   @RequestParam(name = "searchDateLabel", required = false) searchDateLabel: DateType,
                   @RequestParam(name = "searchLabel", required = false) searchLabel: String?,
                   @RequestParam(name = "searchText", required = false) searchText: String?,
                   @RequestParam(name = "effectDate", required = false) effectDate: String?,
                   @RequestParam(name = "expireDate", required = false) expireDate: String?,
                   @RequestParam(name = "ticketType", required = false) ticketType: TicketType?,
                   @RequestParam(name = "delYn", required = false) delYn: String? = "N"): ResponseEntity<CommonResult> {
        logger.debug { "tickets search $searchDateLabel $fromDate $toDate $searchLabel $searchText $ticketType" }
        val criteria = SeasonTicketCriteria(
            searchDateLabel = searchDateLabel, fromDate = DateUtil.stringToLocalDate(fromDate), toDate = DateUtil.stringToLocalDate(toDate), searchLabel = searchLabel, searchText = searchText,
            ticketType = ticketType, delYn = delYn)
        return CommonResult.returnResult(CommonResult.data(seasonTicketQueryService.findByCriteria(criteria)))


    }

}