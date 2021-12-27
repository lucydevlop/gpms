package io.glnt.gpms.handler.discount.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.service.DiscountClassService
import io.glnt.gpms.service.DiscountService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/$API_VERSION/discount/ticket"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DiscountController {

    @Autowired
    private lateinit var discountClassService: DiscountClassService

    @RequestMapping(value = ["/list"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getDiscountClass() : ResponseEntity<CommonResult> {
        logger.trace { "getDiscountClass" }
        return CommonResult.returnResult(CommonResult.data(discountClassService.findAll()))
    }

//    @RequestMapping(value = ["/create"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun createDiscountClass(@RequestBody request: DiscountClass) : ResponseEntity<CommonResult> {
//        logger.trace { "createDiscountClass $request" }
//        val result = discountService.createDiscountClass(request)
//        return when(result.code) {
//            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//        }
//    }

    companion object : KLogging()
}