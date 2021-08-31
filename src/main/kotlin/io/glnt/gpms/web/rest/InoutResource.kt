package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.model.dto.request.resParkInList
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class InoutResource (
    private val inoutService: InoutService
){
    companion object : KLogging()

    @RequestMapping(value = ["/inouts/calc"], method = [RequestMethod.POST])
    fun calc(@Valid @RequestBody resParkInList: resParkInList): ResponseEntity<CommonResult> {
        logger.debug { "inout calc $resParkInList" }
        return CommonResult.returnResult(CommonResult.data(inoutService.calcInout(resParkInList)))
    }

    @RequestMapping(value = ["/inouts"], method = [RequestMethod.PUT])
    fun update(@Valid @RequestBody resParkInList: resParkInList): ResponseEntity<CommonResult> {
        logger.debug { "inout calc $resParkInList" }
        return CommonResult.returnResult(CommonResult.data(inoutService.updateInout(resParkInList)))
    }
}