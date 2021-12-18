package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.model.criteria.SeasonTicketCriteria
import io.glnt.gpms.model.dto.entity.SeasonTicketDTO
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.service.ParkSiteInfoService
import io.glnt.gpms.service.SeasonTicketQueryService
import io.glnt.gpms.service.TicketService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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

    @RequestMapping(value = ["/tickets/season"], method = [RequestMethod.POST])
    fun createSeasonTicket(@Valid @RequestBody ticketDTO: SeasonTicketDTO, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        if (ticketDTO.sn != null) {
            throw CustomException(
                "season-ticket create found sn",
                ResultCode.FAILED
            )
        }

        // 등록 가능한 상품 여부 확인
        if (!checkPeriodSeasonTicket(ticketDTO, "C")) {
            throw CustomException(
                "season-ticket create failed",
                ResultCode.FAILED
            )
        }

        return CommonResult.returnResult(CommonResult.data(ticketService.saveTicket(ticketDTO)))
    }

    @RequestMapping(value = ["/tickets/season"], method = [RequestMethod.PUT])
    fun updateSeasonTicket(@Valid @RequestBody ticketDTO: SeasonTicketDTO, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        if (ticketDTO.sn == null) {
            throw CustomException(
                "season-ticket update not found sn",
                ResultCode.FAILED
            )
        }
        if (!checkPeriodSeasonTicket(ticketDTO, "U")) {
            throw CustomException(
                "season-ticket update failed",
                ResultCode.FAILED
            )
        }

        return CommonResult.returnResult(CommonResult.data(ticketService.saveTicket(ticketDTO)))
    }

    private fun checkPeriodSeasonTicket(ticketDTO: SeasonTicketDTO, mode: String): Boolean {
        ticketService.getTicketByVehicleNoAndTicketTypeAndRangeDate(ticketDTO.vehicleNo?: "", ticketDTO.ticketType?: TicketType.SEASONTICKET, ticketDTO.effectDate?: LocalDateTime.now(), ticketDTO.expireDate?: LocalDateTime.now())?.let { ticketDTOs ->
            if (ticketDTOs.isEmpty()) return true
            if (mode == "U" && ticketDTOs.size == 1) {
                if (ticketDTOs[0].sn == ticketDTO.sn) return true
            }
            return false
        }?: run { return true }
    }

    @RequestMapping(value = ["/tickets/seasons"], method = [RequestMethod.POST])
    fun createSeasonTickets(@Valid @RequestBody ticketDTOs: ArrayList<SeasonTicketDTO>, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        ticketDTOs.forEach { seasonTicketDTO ->
            //sn 이 있을 경우  update
            if (seasonTicketDTO.sn != null) {
                if (checkPeriodSeasonTicket(seasonTicketDTO, "U")) {
                    ticketService.saveTicket(seasonTicketDTO)
                }
            } else {
                if (checkPeriodSeasonTicket(seasonTicketDTO, "C")) {
                    ticketService.saveTicket(seasonTicketDTO)
                }
            }
        }
        return CommonResult.returnResult(CommonResult.data())
    }

    @RequestMapping(value = ["/tickets/free"], method = [RequestMethod.POST])
    fun createFreeTicket(@Valid @RequestBody ticketDTO: SeasonTicketDTO, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        if (ticketDTO.sn != null) {
            throw CustomException(
                "free-ticket create found sn",
                ResultCode.FAILED
            )
        }

        // 등록 가능한 상품 여부 확인
        if (!checkPeriodFreeTicket(ticketDTO, "C")) {
            throw CustomException(
                "free-ticket create failed",
                ResultCode.FAILED
            )
        }

        return CommonResult.returnResult(CommonResult.data(ticketService.saveTicket(ticketDTO)))
    }

    @RequestMapping(value = ["/tickets/free"], method = [RequestMethod.PUT])
    fun updateFreeTicket(@Valid @RequestBody ticketDTO: SeasonTicketDTO, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        if (ticketDTO.sn == null) {
            throw CustomException(
                "free-ticket update not found sn",
                ResultCode.FAILED
            )
        }
        if (!checkPeriodFreeTicket(ticketDTO, "U")) {
            throw CustomException(
                "free-ticket update failed",
                ResultCode.FAILED
            )
        }

        return CommonResult.returnResult(CommonResult.data(ticketService.saveTicket(ticketDTO)))
    }

    @RequestMapping(value = ["/tickets/frees"], method = [RequestMethod.POST])
    fun createFreeTickets(@Valid @RequestBody ticketDTOs: ArrayList<SeasonTicketDTO>, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        ticketDTOs.forEach { seasonTicketDTO ->
            //sn 이 있을 경우  update
            if (seasonTicketDTO.sn != null) {
                if (checkPeriodFreeTicket(seasonTicketDTO, "U")) {
                    ticketService.saveTicket(seasonTicketDTO)
                }
            } else {
                if (checkPeriodFreeTicket(seasonTicketDTO, "C")) {
                    ticketService.saveTicket(seasonTicketDTO)
                }
            }
        }
        return CommonResult.returnResult(CommonResult.data())
    }

    private fun checkPeriodFreeTicket(ticketDTO: SeasonTicketDTO, mode: String): Boolean {
        ticketService.getTicketByVehicleNoAndTicketTypeAndRangeDate(ticketDTO.vehicleNo?: "", ticketDTO.ticketType?: TicketType.FREETICKET, ticketDTO.effectDate?: LocalDateTime.now(), ticketDTO.expireDate?: LocalDateTime.now())?.let { ticketDTOs ->
            if (ticketDTOs.isEmpty()) return true
            if (mode == "U" && ticketDTOs.size == 1) {
                if (ticketDTOs[0].sn == ticketDTO.sn) return true
            }
            return false
        }?: run { return true }
    }

    @RequestMapping(value = ["/tickets/visit"], method = [RequestMethod.POST])
    fun createVisitTicket(@Valid @RequestBody ticketDTO: SeasonTicketDTO, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        if (ticketDTO.sn != null) {
            throw CustomException(
                "free-ticket create found sn",
                ResultCode.FAILED
            )
        }

        // 등록 가능한 상품 여부 확인
        if (!checkPeriodFreeTicket(ticketDTO, "C")) {
            throw CustomException(
                "free-ticket create failed",
                ResultCode.FAILED
            )
        }

        return CommonResult.returnResult(CommonResult.data(ticketService.saveTicket(ticketDTO)))
    }

    @RequestMapping(value = ["/tickets/visit"], method = [RequestMethod.PUT])
    fun updateVisitTicket(@Valid @RequestBody ticketDTO: SeasonTicketDTO, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        if (ticketDTO.sn == null) {
            throw CustomException(
                "free-ticket update not found sn",
                ResultCode.FAILED
            )
        }
        if (!checkPeriodFreeTicket(ticketDTO, "U")) {
            throw CustomException(
                "free-ticket update failed",
                ResultCode.FAILED
            )
        }

        return CommonResult.returnResult(CommonResult.data(ticketService.saveTicket(ticketDTO)))
    }

    @RequestMapping(value = ["/tickets/visits"], method = [RequestMethod.POST])
    fun createVisitTickets(@Valid @RequestBody ticketDTOs: ArrayList<SeasonTicketDTO>, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
        ticketDTOs.forEach { seasonTicketDTO ->
            //sn 이 있을 경우  update
            if (seasonTicketDTO.sn != null) {
                if (checkPeriodFreeTicket(seasonTicketDTO, "U")) {
                    ticketService.saveTicket(seasonTicketDTO)
                }
            } else {
                if (checkPeriodFreeTicket(seasonTicketDTO, "C")) {
                    ticketService.saveTicket(seasonTicketDTO)
                }
            }
        }
        return CommonResult.returnResult(CommonResult.data())
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

    @RequestMapping(value = ["/extend/tickets"], method = [RequestMethod.GET])
    fun extendTickets() : ResponseEntity<CommonResult> {
        ticketService.extendSeasonTicket()
        return CommonResult.returnResult(CommonResult.data())
    }

}