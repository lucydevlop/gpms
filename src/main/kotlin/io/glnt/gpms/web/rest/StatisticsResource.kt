package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.service.StatisticsService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class StatisticsResource (
    private val statisticsService: StatisticsService
){
    companion object : KLogging()

    @RequestMapping(value = ["/statistic/inout/count/day"], method = [RequestMethod.GET])
    fun getInoutCountByDays(@RequestParam startDate: String, @RequestParam endDate: String) : ResponseEntity<CommonResult> {
        // 일자별 입차 / 일반차량(입차기준) / 정기차량(입차기준)
        return CommonResult.returnResult(CommonResult.data(statisticsService.getInoutCountByDays(startDate, endDate)))
    }

    @RequestMapping(value = ["/statistic/inout/count/month"], method = [RequestMethod.GET])
    fun getInoutCountByMonths(@RequestParam startDate: String, @RequestParam endDate: String) : ResponseEntity<CommonResult> {
        // 일자별 입차 / 일반차량(입차기준) / 정기차량(입차기준) / 출차 / 주차금액 / 할인금액 / 결제금액
        return CommonResult.returnResult(CommonResult.data(statisticsService.getInoutCountByMonths(startDate, endDate)))
    }

    @RequestMapping(value = ["/statistic/inout/payment/day"], method = [RequestMethod.GET])
    fun getInoutPaymentByDays(@RequestParam startDate: String, @RequestParam endDate: String) : ResponseEntity<CommonResult> {
        // 일자별 / 주차금액 / 할인금액 / 결제금액
        return CommonResult.returnResult(CommonResult.data(statisticsService.getInoutPaymentByDays(startDate, endDate)))
    }

    @RequestMapping(value = ["/statistic/inout/payment/month"], method = [RequestMethod.GET])
    fun getInoutPaymentByMonths(@RequestParam startDate: String, @RequestParam endDate: String) : ResponseEntity<CommonResult> {
        // 일자별 / 주차금액 / 할인금액 / 결제금액
        return CommonResult.returnResult(CommonResult.data(statisticsService.getInoutPaymentByMonths(startDate, endDate)))
    }
}