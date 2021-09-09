package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.GateDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.service.FacilityService
import io.glnt.gpms.service.GateService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class GateResource (
    private val gateService: GateService,
    private val facilityService: FacilityService
){
    companion object : KLogging()

    @RequestMapping(value = ["/gates"], method = [RequestMethod.GET])
    fun findAll(): ResponseEntity<CommonResult> {
        logger.debug { "gate fetch all" }
        return CommonResult.returnResult(CommonResult.data())
    }

    @RequestMapping(value = ["/gates"], method = [RequestMethod.PUT])
    fun update(@Valid @RequestBody gateDTO: GateDTO): ResponseEntity<CommonResult> {
        if (gateDTO.sn == null) {
            throw CustomException(
                "gate update not found sn",
                ResultCode.FAILED
            )
        }
        val gate = gateService.saveGate(gateDTO)
        // 시설 상태 정보 update
        facilityService.findByGateId(gate.gateId ?: "")?.let { facilities ->
            facilities.forEach { facilityDTO ->
                facilityDTO.delYn = gate.delYn
                facilityService.save(facilityDTO)
            }
        }

        return CommonResult.returnResult(CommonResult.data(gate))
    }

    @RequestMapping(value = ["/gate"], method = [RequestMethod.POST])
    fun create(@Valid @RequestBody gateDTO: GateDTO): ResponseEntity<CommonResult> {
        if (gateDTO.sn != null) {
            throw CustomException(
                "gate create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(gateService.saveGate(gateDTO)))
    }
}