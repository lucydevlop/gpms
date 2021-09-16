package io.glnt.gpms.handler.calc.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.model.reqCalc
import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}/calc"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CalcController {
    @Autowired
    private lateinit var feeCalculation: FeeCalculation

    @Autowired
    private lateinit var fareRefService: FareRefService

//    @RequestMapping(value = ["/fare/info"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun createFareInfo(@RequestBody request: FareInfo) : ResponseEntity<CommonResult> {
//        logger.trace { "createFareInfo : $request" }
//        val result = fareRefService.createFareInfo(request)
//        return when(result.code) {
//            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//        }
//    }

//    @RequestMapping(value = ["/fare/policy"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun createFarePolicy(@RequestBody request: FarePolicy) : ResponseEntity<CommonResult> {
//        logger.trace { "createFareInfo : $request" }
//        val result = fareRefService.createFarePolicy(request)
//        return when(result.code) {
//            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//        }
//    }

    @RequestMapping(value = ["/inout"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun calcInout(@RequestBody request: reqCalc) : ResponseEntity<*> {
        feeCalculation.init()
        return ResponseEntity(feeCalculation.getBasicPayment(request.inTime, request.outTime, request.vehicleType, request.vehicleNo, request.type, request.discountMin, request.inSn), HttpStatus.OK)

    }

    companion object : KLogging()
}