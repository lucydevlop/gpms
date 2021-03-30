package io.glnt.gpms.handler.relay.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/relay"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class RelayController {
    @Autowired
    private lateinit var relayService: RelayService

    @RequestMapping(value=["/health_check"], method=[RequestMethod.POST])
    fun healthCheck(@RequestBody request: reqRelayHealthCheck) {
        logger.debug { "healthCheck category $request" }
        relayService.facilitiesHealthCheck(request)
    }

    @RequestMapping(value=["/failure_alarm"], method=[RequestMethod.POST])
    fun failureAlarm(@RequestBody request: reqRelayHealthCheck) {
        logger.debug { "healthCheck category $request" }
        relayService.failureAlarm(request)
    }

    @RequestMapping(value=["/status_noti"], method=[RequestMethod.POST])
    fun statusNoti(@RequestBody request: reqRelayHealthCheck) {
        logger.debug { "statusNoti category $request" }
        relayService.statusNoti(request)
    }

    @RequestMapping(value=["/payment_health"], method=[RequestMethod.GET])
    fun paymentHealthCheck() {
        logger.debug { "paymentHealthCheck " }
        relayService.paymentHealthCheck()
    }

    @RequestMapping(value=["/paystation/result/{dtFacilityId}"], method=[RequestMethod.POST])
    fun resultPayment(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String) {
        logger.info { "resultPayment $request " }
        relayService.resultPayment(request, dtFacilityId)
    }

    @RequestMapping(value = ["/paystation/search/vehicle/{dtFacilityId}"], method = [RequestMethod.POST])
    fun searchCarNumber(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String) {
        logger.info { "searchCarNumber $request " }
        relayService.searchCarNumber(request, dtFacilityId)
    }

    @RequestMapping(value = ["/paystation/request/adjustment/{dtFacilityId}"], method = [RequestMethod.POST])
    fun requestAdjustment(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String) {
        logger.info { "requestAdjustment $request " }
        relayService.requestAdjustment(request, dtFacilityId)
    }

    @RequestMapping(value = ["/display/init/message"], method = [RequestMethod.GET])
    fun sendDisplayInitMessage(): ResponseEntity<CommonResult> {
        logger.info { "sendDisplayInitMessage" }
        return CommonResult.returnResult(relayService.sendDisplayInitMessage())
    }

    companion object : KLogging()
}