package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.model.dto.entity.FacilityDTO
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.service.FacilityService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class FacilityResource(
    private val facilityService: FacilityService,
    private val rcsService: RcsService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/facilities"], method = [RequestMethod.GET])
    fun findAll(): ResponseEntity<CommonResult> {
        logger.debug { "facility fetch all" }
        return CommonResult.returnResult(CommonResult.data(facilityService.allFacilities()))
    }

    @RequestMapping(value = ["/facilities/active"], method = [RequestMethod.GET])
    fun getActiveFacilities(): ResponseEntity<CommonResult> {
        logger.debug { "facility fetch active" }
        return CommonResult.returnResult(CommonResult.data(facilityService.activeGateFacilities()))
    }

    @RequestMapping(value=["/facilities/action/{facilityId}/{status}"], method = [RequestMethod.GET])
    fun facilityAction(@PathVariable facilityId: String, @PathVariable status: String): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.facilityAction(facilityId, status))
    }

    @RequestMapping(value=["/facilities/{sn}"], method = [RequestMethod.PUT])
    fun update(@Valid @PathVariable sn: Long, @RequestBody facilityDTO: FacilityDTO): ResponseEntity<CommonResult> {
        logger.debug { "facility update $facilityDTO" }
        if (facilityDTO.sn == null) {
            throw CustomException(
                "facility update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(facilityService.save(facilityDTO)))
    }

    @RequestMapping(value=["/facilities/{sn}"], method = [RequestMethod.DELETE])
    fun delete(@Valid @PathVariable sn: Long): ResponseEntity<CommonResult> {
        logger.debug { "facility delete $sn" }
        facilityService.findBySn(sn)
            ?.let { facilityDTO ->
                facilityDTO.delYn = YN.Y
                return CommonResult.returnResult(CommonResult.data(facilityService.save(facilityDTO)))
            }
            ?: throw CustomException(
                "facility update not found sn",
                ResultCode.FAILED
            )
    }

    @RequestMapping(value=["/facilities"], method = [RequestMethod.POST])
    fun create(@RequestBody facilityDTO: FacilityDTO): ResponseEntity<CommonResult> {
        logger.debug { "facility update $facilityDTO" }
        if (facilityDTO.sn != null) {
            throw CustomException(
                "facility update sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(facilityService.save(facilityDTO)))
    }
}