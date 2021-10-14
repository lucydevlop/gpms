package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.dto.HolidayDTO
import io.glnt.gpms.service.HolidayService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class HolidayResource (
    private val holidayService: HolidayService
) {
    companion object : KLogging()

    @RequestMapping(value = ["/holidays"], method = [RequestMethod.GET])
    fun getHolidays(@RequestParam startDate: String, @RequestParam endDate: String): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(holidayService.findByDays(DateUtil.stringToLocalDate(startDate), DateUtil.stringToLocalDate(endDate))))
    }

    @RequestMapping(value = ["/holiday"], method = [RequestMethod.POST])
    fun create(@RequestBody holidayDTO: HolidayDTO): ResponseEntity<CommonResult> {
        if (holidayDTO.sn != null) {
            throw CustomException(
                "holiday create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(holidayService.save(holidayDTO)))
    }

    @RequestMapping(value = ["/holiday"], method = [RequestMethod.PUT])
    fun update(@RequestBody holidayDTO: HolidayDTO): ResponseEntity<CommonResult> {
        if (holidayDTO.sn == null) {
            throw CustomException(
                "holiday update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(holidayService.save(holidayDTO)))
    }
}