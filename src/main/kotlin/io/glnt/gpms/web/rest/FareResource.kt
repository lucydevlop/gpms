package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.FareInfoDTO
import io.glnt.gpms.model.dto.FarePolicyDTO
import io.glnt.gpms.service.FareService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class FareResource (
    private val fareService: FareService
){
    companion object : KLogging()

    @RequestMapping(value = ["/fare/policy"], method = [RequestMethod.POST])
    fun createFarePolicy(@Valid @RequestBody farePolicyDTO: FarePolicyDTO) : ResponseEntity<CommonResult> {
        if (farePolicyDTO.sn != null) {
            throw CustomException(
                "Fare Policy create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(fareService.saveFarePolicy(farePolicyDTO)))
    }

    @RequestMapping(value = ["/fare/policy"], method = [RequestMethod.PUT])
    fun updateFarePolicy(@Valid @RequestBody farePolicyDTO: FarePolicyDTO) : ResponseEntity<CommonResult> {
        if (farePolicyDTO.sn == null) {
            throw CustomException(
                "Fare Policy not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(fareService.saveFarePolicy(farePolicyDTO)))
    }

    @RequestMapping(value = ["/fare/info"], method = [RequestMethod.POST])
    fun createFareInfo(@Valid @RequestBody fareInfoDTO: FareInfoDTO) : ResponseEntity<CommonResult> {
        if (fareInfoDTO.sn != null) {
            throw CustomException(
                "Fare Info create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(fareService.saveFareInfo(fareInfoDTO)))
    }

    @RequestMapping(value = ["/fare/info"], method = [RequestMethod.PUT])
    fun updateFareInfo(@Valid @RequestBody fareInfoDTO: FareInfoDTO) : ResponseEntity<CommonResult> {
        if (fareInfoDTO.sn == null) {
            throw CustomException(
                "Fare Info not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(fareService.saveFareInfo(fareInfoDTO)))
    }




}