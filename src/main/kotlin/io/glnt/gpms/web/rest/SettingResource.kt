package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class SettingResource {
    companion object : KLogging()

    @GetMapping(value = ["/settings"])
    @ResponseStatus(HttpStatus.OK)
    fun getAll(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data())
    }


}