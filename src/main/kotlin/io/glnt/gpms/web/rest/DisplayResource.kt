package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.entity.DisplayMessageDTO
import io.glnt.gpms.service.DisplayService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DisplayResource (
    private val displayService: DisplayService
){
    companion object : KLogging()

    @RequestMapping(value = ["/display/message"], method = [RequestMethod.PUT])
    fun updateDisplayMessage(@Valid @RequestBody displayMessageDTO: DisplayMessageDTO): ResponseEntity<CommonResult> {
        if (displayMessageDTO.sn == null) {
            throw CustomException(
                "display message update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(displayService.saveDisplayMessage(displayMessageDTO)))
    }

    @RequestMapping(value = ["/display/message"], method = [RequestMethod.POST])
    fun createDisplayMessage(@Valid @RequestBody displayMessageDTO: DisplayMessageDTO): ResponseEntity<CommonResult> {
        if (displayMessageDTO.sn != null) {
            throw CustomException(
                "display message create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(displayService.saveDisplayMessage(displayMessageDTO)))
    }
}
