package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.GateDTO
import io.glnt.gpms.model.dto.TicketClassDTO
import io.glnt.gpms.service.TicketClassService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class TicketClassResource(
    private val ticketClassService: TicketClassService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/ticket/classes"], method = [RequestMethod.GET])
    fun getTicketClasses(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(ticketClassService.findAll()))
    }

    @RequestMapping(value = ["/ticket/classes"], method = [RequestMethod.PUT])
    fun update(@Valid @RequestBody ticketClassDTO: TicketClassDTO): ResponseEntity<CommonResult> {
        if (ticketClassDTO.sn == null) {
            throw CustomException(
                "ticket-class update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(ticketClassService.save(ticketClassDTO)))
    }

    @RequestMapping(value = ["/ticket/classes"], method = [RequestMethod.POST])
    fun create(@Valid @RequestBody ticketClassDTO: TicketClassDTO): ResponseEntity<CommonResult> {
        if (ticketClassDTO.sn != null) {
            throw CustomException(
                "ticket-class create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(ticketClassService.save(ticketClassDTO)))
    }
}