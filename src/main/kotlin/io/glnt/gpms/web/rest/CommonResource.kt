package io.glnt.gpms.io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import mu.KLogging
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CommonResource(
    private val env: Environment
) {
    companion object : KLogging()

    @RequestMapping(value = ["/version"], method = [RequestMethod.GET])
    fun getVersion(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(env.getProperty("spring.application.version")))
    }
}