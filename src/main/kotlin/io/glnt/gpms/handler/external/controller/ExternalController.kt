package io.glnt.gpms.io.glnt.gpms.handler.external.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.exception.CustomException
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/external"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ExternalController {
    @RequestMapping(value = ["/ticket"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getTickets() : ResponseEntity<CommonResult> {
        logger.trace("getTickets")
        val credentials = Base64Util.encodeAsString("api-user:glnt11!!")
        return CommonResult.returnResult(CommonResult.data(credentials))
    }
    companion object : KLogging()
}