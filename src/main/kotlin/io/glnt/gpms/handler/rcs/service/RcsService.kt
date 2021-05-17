package io.glnt.gpms.handler.rcs.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.facility.service.FacilityService
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class RcsService(
    private var facilityService: FacilityService
) {
    companion object : KLogging()

    fun asyncFacilities(): CommonResult {
        return CommonResult.data(facilityService.activeGateFacilities())
    }
}