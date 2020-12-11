package io.glnt.gpms.handler.parkinglot.controller

import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.handler.parkinglot.model.reqAddParkIn
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.parkinglot.model.reqAddParkinglotFeature
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(
    path = ["/${API_VERSION}/parkinglot"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ParkinglotController {

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @RequestMapping(value = ["/facility/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkinglotfacilities(@RequestBody request: reqSearchParkinglotFeature): ResponseEntity<CommonResult> {
        logger.debug("parkinglot facility list  = $request")
        val result = parkinglotService.getParkinglotfacilities(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/feature/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkinglotFeature(@RequestBody request: reqSearchParkinglotFeature): ResponseEntity<CommonResult> {
        logger.debug("parkinglot feature list  = $request")
        val result = parkinglotService.getParkinglotFeature(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/feature/add"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun addParkinglotFeature(@RequestBody request: reqAddParkinglotFeature): ResponseEntity<CommonResult> {
        logger.debug("list parkinglot = $request")
        val result = parkinglotService.addParkinglotFeature(request)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/parkin"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun parkIn(@RequestBody request: reqAddParkIn) : ResponseEntity<CommonResult> {
        val result = parkinglotService.parkIn(request)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }

    }

//    @RequestMapping(value = ["/add"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun addVehicle(json: String): CommonResult<Int>? {
//        logger.debug("add vehicle = $json")
//        val result = CommonResult<Int>()
//        val rtn: Int = vehicleService.addVehicle(json)
//        result.setRespData(rtn)
//        return result
//    }

//    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun getAllParkinglot(@RequestBody request: reqSearchParkinglot): CommonResult<Objects>? {
//        logger.debug("list parkinglot = $request")
//        val result = CommonResult<Objects>()
//        return result
//
//    }

    companion object : KLogging()
}