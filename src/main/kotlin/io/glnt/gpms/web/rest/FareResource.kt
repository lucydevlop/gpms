package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.entity.CgBasicDTO
import io.glnt.gpms.model.dto.entity.FareInfoDTO
import io.glnt.gpms.model.dto.entity.FarePolicyDTO
import io.glnt.gpms.model.dto.rcs.RcsRateInfoDTO
import io.glnt.gpms.model.dto.response.FareReferenceDTO
import io.glnt.gpms.model.enums.YN
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

    @RequestMapping(value = ["/charge/references"], method = [RequestMethod.GET])
    fun getFareReference(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(
            CommonResult.data(
                FareReferenceDTO(
                    fareInfos = fareService.findFareInfo(),
                    farePolicies = fareService.findFarePolicies().filter { farePolicyDTO -> farePolicyDTO.delYn == YN.N },
                    fareBasic = fareService.findFareBasic()
                )
            )
        )
    }

    @RequestMapping(value = ["/fare/policies"], method = [RequestMethod.GET])
    fun getFarePolicies(): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(fareService.findFarePolicies().filter { farePolicyDTO -> farePolicyDTO.delYn == YN.N }))
    }

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

    @RequestMapping(value = ["/charge/info"], method = [RequestMethod.POST])
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

    @RequestMapping(value = ["/fares"], method = [RequestMethod.GET])
    fun getFares(): ResponseEntity<CommonResult> {
        val fareBasic = fareService.findFareBasic()
        val farePolicies = fareService.findFarePolicies()
        return CommonResult.returnResult(CommonResult.data(RcsRateInfoDTO(fareBasic = fareBasic, farePolicies = farePolicies)))
    }

//    @RequestMapping(value = ["/charge/basic"], method = [RequestMethod.PUT])
//    fun updateFareBasic(@Valid @RequestBody cgBasicDTO: CgBasicDTO) : ResponseEntity<CommonResult> {
//        if (cgBasicDTO.sn == null) {
//            throw CustomException(
//                "Fare Basic not found sn",
//                ResultCode.FAILED
//            )
//        }
//        return CommonResult.returnResult(CommonResult.data(fareService.saveFareBasic(cgBasicDTO)))
//    }

    @RequestMapping(value = ["/charge/basic"], method = [RequestMethod.POST])
    fun createFareBasic(@Valid @RequestBody cgBasicDTO: CgBasicDTO) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(fareService.saveFareBasic(cgBasicDTO)))
    }
}