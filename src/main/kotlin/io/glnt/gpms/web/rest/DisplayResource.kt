package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.entity.DisplayMessageDTO
import io.glnt.gpms.model.entity.DisplayInfo
import io.glnt.gpms.model.enums.YN
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

    @RequestMapping(value = ["/displays"], method = [RequestMethod.GET])
    fun getDisplays(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(displayService.getAll()))
    }

    @RequestMapping(value = ["/displays/message/{sn}"], method = [RequestMethod.PUT])
    fun updateDisplayMessage(@PathVariable sn: Long, @Valid @RequestBody displayMessageDTO: DisplayMessageDTO): ResponseEntity<CommonResult> {
        if (displayMessageDTO.sn == null) {
            throw CustomException(
                "display message update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(displayService.saveDisplayMessage(displayMessageDTO)))
    }

    @RequestMapping(value = ["/displays/message/{sn}"], method = [RequestMethod.DELETE])
    fun deleteDisplayMessage(@PathVariable sn: Long): ResponseEntity<CommonResult> {
        displayService.findDisplayMessageBySn(sn)
            ?.let { displayMessageDTO ->
                displayMessageDTO.delYn = YN.Y
                return CommonResult.returnResult(CommonResult.data(displayService.saveDisplayMessage(displayMessageDTO)))
            }
            ?: throw CustomException(
                "display message update not found sn",
                ResultCode.FAILED
            )
    }

    @RequestMapping(value = ["/displays/message"], method = [RequestMethod.POST])
    fun createDisplayMessage(@Valid @RequestBody displayMessageDTO: DisplayMessageDTO): ResponseEntity<CommonResult> {
        if (displayMessageDTO.sn != null) {
            throw CustomException(
                "display message create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(displayService.saveDisplayMessage(displayMessageDTO)))
    }

    @RequestMapping(value=["/displays/info/{sn}"], method = [RequestMethod.PUT])
    fun updateDisplayInfo(@PathVariable sn: Long, @Valid @RequestBody displayInfo: DisplayInfo): ResponseEntity<CommonResult> {
        if (displayInfo.sn == null) {
            throw CustomException(
                "display info update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(displayService.saveDisplayInfo(displayInfo)))
    }
}
