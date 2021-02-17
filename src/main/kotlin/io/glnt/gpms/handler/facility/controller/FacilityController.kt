package io.glnt.gpms.handler.facility.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.facility.model.reqPayStationData
import io.glnt.gpms.handler.facility.model.reqSetDisplayColor
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.handler.tmap.model.reqSendVehicleListSearch
import io.glnt.gpms.handler.facility.model.reqUpdateFacilities
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.websocket.server.PathParam

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/facility"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class FacilityController {

    @Autowired
    private lateinit var facilityService: FacilityService

    @RequestMapping(value= ["/display/color"], method = [RequestMethod.GET])
    fun getDisplayColor() : ResponseEntity<CommonResult> {
        logger.trace { "getDisplayColor" }
        val result = facilityService.getDisplayColor()
        return when (result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value= ["/display/message"], method = [RequestMethod.GET])
    fun getDisplayMessage() : ResponseEntity<CommonResult> {
        logger.trace { "getDisplayMessage" }
        val result = facilityService.getDisplayMessage()
        return when (result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value= ["/display/color"], method = [RequestMethod.POST])
    fun setDisplayColor(@RequestBody request: ArrayList<reqSetDisplayColor>) : ResponseEntity<CommonResult> {
        logger.debug("parkinglot facility display color : $request")
        val result = facilityService.setDisplayColor(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value= ["/display/message"], method = [RequestMethod.POST])
    fun setDisplayMessage(@RequestBody request: ArrayList<reqSetDisplayMessage>) : ResponseEntity<CommonResult> {
        logger.debug("parkinglot facility display message : $request")
        val result = facilityService.setDisplayMessage(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

//    @RequestMapping(value = ["/paystation/search"])
//    fun searchCarNumber(@RequestBody request: reqSendVehicleListSearch) : ResponseEntity<CommonResult> {
//        logger.debug("parkinglot facility paystation search param : $request")
//
//        val result = facilityService.searchCarNumber(request)
//        return when(result!!.code) {
//            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//        }
//    }

    @RequestMapping(value = ["/paystation/payment/{facilitiesId}"])
    fun payment(@RequestBody request: reqApiTmapCommon, @PathVariable("facilitiesId") facilitiesId: String) : ResponseEntity<CommonResult> {
        logger.debug("parkinglot facility paystation search param : $request")

        val result = facilityService.sendPayment(request, facilitiesId)
        return when(result!!.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/all/update"])
    fun allUpdateFacilities(@RequestBody request: reqUpdateFacilities): ResponseEntity<CommonResult> {
        logger.trace("parkinglot facility update : $request")

        val result = facilityService.allUpdateFacilities(request)
        return when (result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    companion object : KLogging()
}