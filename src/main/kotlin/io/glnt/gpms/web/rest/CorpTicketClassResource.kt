package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.entity.CorpTicketClassDTO
import io.glnt.gpms.service.CorpTicketClassService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CorpTicketClassResource (
    private val corpTicketClassService: CorpTicketClassService
){
    companion object : KLogging()

    @RequestMapping(value = ["/corp/ticket/classes"], method = [RequestMethod.GET])
    fun getCorpTicketClasses(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(corpTicketClassService.findAll()))
    }

    @RequestMapping(value = ["/corp/ticket/classes"], method = [RequestMethod.POST])
    fun create(@Valid @RequestBody corpTicketClassDTO: CorpTicketClassDTO): ResponseEntity<CommonResult> {
        if (corpTicketClassDTO.sn != null) {
            throw CustomException(
                "corp-ticket-class create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpTicketClassService.save(corpTicketClassDTO)))
    }

    @RequestMapping(value = ["/corp/ticket/classes"], method = [RequestMethod.PUT])
    fun update(@Valid @RequestBody corpTicketClassDTO: CorpTicketClassDTO): ResponseEntity<CommonResult> {
        if (corpTicketClassDTO.sn == null) {
            throw CustomException(
                "corp-ticket-class update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpTicketClassService.save(corpTicketClassDTO)))
    }
}