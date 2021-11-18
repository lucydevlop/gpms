package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.model.dto.ProductTicketDTO
import io.glnt.gpms.service.ParkSiteInfoService
import io.glnt.gpms.service.TicketService
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
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
    private val authService: AuthService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/tickets/visitors"], method = [RequestMethod.POST])
    fun createVisitors(@Valid @RequestBody ticketDTOs: ArrayList<ProductTicketDTO>, servlet: HttpServletRequest): ResponseEntity<CommonResult> {
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

}