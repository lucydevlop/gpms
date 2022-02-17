package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.model.dto.entity.DiscountClassDTO
import io.glnt.gpms.service.DiscountClassService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DiscountClassResource (
    private val discountClassService: DiscountClassService

){
    companion object : KLogging()

    @RequestMapping(value = ["/discounts/classes"], method = [RequestMethod.GET])
    fun getDiscountClasses(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(discountClassService.findAll()))
    }

    @RequestMapping(value = ["/discounts/classes/create"], method = [RequestMethod.POST])
    fun create(@Valid @RequestBody discountClassDTO: DiscountClassDTO): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(discountClassService.save(discountClassDTO)))
    }
}