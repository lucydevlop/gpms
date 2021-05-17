package io.glnt.gpms.handler.rcs.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.handler.rcs.service.RcsService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

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

}