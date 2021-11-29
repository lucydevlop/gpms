package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ErrorResource {
    companion object : KLogging()

    @RequestMapping(value = ["/errors"], method = [RequestMethod.GET])
    fun getErrors(@RequestParam(name = "fromDate", required = false) fromDate: String,
                  @RequestParam(name = "toDate", required = false) toDate: String): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data())
    }
}