package io.glnt.gpms.handler.facility.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.facility.model.reqSetDisplayColor
import io.glnt.gpms.handler.facility.service.FacilityService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/facility"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class FacilityController {

    @Autowired
    private lateinit var facilityService: FacilityService

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


    companion object : KLogging()
}