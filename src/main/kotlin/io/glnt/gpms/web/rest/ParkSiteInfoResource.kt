package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.model.dto.entity.ParkSiteInfoDTO
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ParkSiteInfoResource(
    private val parkSiteInfoService: ParkSiteInfoService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/parking-lots"], method = [RequestMethod.GET])
    fun findAll(): ResponseEntity<CommonResult> {
        logger.debug { "parkinglots fetch all" }
        return CommonResult.returnResult(CommonResult.data(parkSiteInfoService.find()))
    }

    @RequestMapping(value = ["/parking-lots"], method = [RequestMethod.POST])
    fun create(@RequestBody parkSiteInfoDTO: ParkSiteInfoDTO): ResponseEntity<CommonResult> {
        parkSiteInfoService.find()?.let {
            if (parkSiteInfoDTO.siteId != it.siteId)
                parkSiteInfoService.delete(it)
        }
        val parkSiteInfo = parkSiteInfoService.save(parkSiteInfoDTO)
        return CommonResult.returnResult(CommonResult.data(parkSiteInfo))
    }
}