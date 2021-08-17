package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.service.DiscountClassService
import mu.KLogging
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
class DiscountClassResource (
    private val discountClassService: DiscountClassService

){
    companion object : KLogging()

    @RequestMapping(value = ["/discount/classes"], method = [RequestMethod.GET])
    fun getDiscountClasses(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(discountClassService.findAll()))
    }
}