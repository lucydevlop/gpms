package io.glnt.gpms.handler.corp.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.parkinglot.controller.ParkinglotController
import io.glnt.gpms.handler.corp.service.CorpService
import io.glnt.gpms.handler.dashboard.admin.model.reqSearchCorp
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.model.entity.Corp
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}/corp"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CorpController {

    @Autowired
    private lateinit var corpService: CorpService

    @Autowired
    private lateinit var discountService: DiscountService

    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getCorp(@RequestBody request: reqSearchCorp) : ResponseEntity<CommonResult> {
        logger.debug("list corp = $request")
        val result = corpService.getCorp(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/delete/{id}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteCorp(@PathVariable id: Long): ResponseEntity<CommonResult> {
        logger.trace { "deleteCorp : $id" }
        val result = corpService.deleteCorp(id)
        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createCorp(@RequestBody request: Corp) : ResponseEntity<CommonResult> {
        logger.trace { "createCorp : $request" }
        val result = corpService.createCorp(request)
        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

//    @RequestMapping(value = ["/ticket"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun getCorpTicket(@RequestBody request: reqSearchDiscount) : ResponseEntity<CommonResult> {
//        logger.debug("list corp = $request")
//        val result = discountService.getByCorp(request)
//
//        return when(result.code) {
//            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//
//        }
//    }


    companion object : KLogging()


}