package io.glnt.gpms.handler.relay.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.service.BarcodeClassService
import io.glnt.gpms.service.BarcodeService
import io.glnt.gpms.service.BarcodeTicketService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/relay"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class RelayController (
    private val barcodeTicketService: BarcodeTicketService,
//    private val barcodeService: BarcodeService,
//    private val barcodeClassService: BarcodeClassService,
//    private val discountService: DiscountService
){
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

    @RequestMapping(value=["/paystation/aply/discount/{dtFacilityId}"], method = [RequestMethod.POST])
    fun aplyBarcode(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String) {
        logger.info { "aplyBarcode request $request" }

//        if (barcodeTicketsDTO.sn != null) {
//            return CommonResult.returnResult(CommonResult.error("barcode bad request"))
//        }
//
//        val info = barcodeService.findAll().filter {
//            it.effectDate!! <= LocalDateTime.now() && it.expireDate!! >= LocalDateTime.now() && it.delYn == DelYn.N}.get(0)
//
//        barcodeTicketsDTO.price = barcodeTicketsDTO.barcode?.let { it.substring(info.startIndex!!, info.endIndex!!).toInt() }
//
//        barcodeClassService.findByStartLessThanEqualAndEndGreaterThanAndDelYn(barcodeTicketsDTO.price!!)?.let { barcodeClass ->
//            // 입차할인권 save
//            barcodeTicketsDTO.inSn?.let {
//                discountService.saveInoutDiscount(
//                    InoutDiscount(
//                        sn = null, discontType = TicketType.BARCODE, discountClassSn = barcodeClass.discountClassSn, inSn = it,
//                        quantity = 1, useQuantity = 1, delYn = DelYn.N, applyDate = barcodeTicketsDTO.applyDate))
//            }
//        }
//        barcodeTicketsDTO.delYn = DelYn.N
//        return CommonResult.returnResult(CommonResult.data(barcodeTicketService.save(barcodeTicketsDTO)))
        relayService.aplyDiscountTicket(request, dtFacilityId)
    }

    @RequestMapping(value = ["/display/init/message"], method = [RequestMethod.GET])
    fun sendDisplayInitMessage(): ResponseEntity<CommonResult> {
        logger.info { "sendDisplayInitMessage" }
        return CommonResult.returnResult(relayService.sendDisplayInitMessage())
    }

    @RequestMapping(value = ["/display/info"], method = [RequestMethod.GET])
    fun sendDisplayInfo(): ResponseEntity<CommonResult> {
        logger.info { "sendDisplayInitMessage" }
        return CommonResult.returnResult(relayService.sendDisplayInfo())
    }

    @RequestMapping(value = ["/call/voip/{voipId}"], method = [RequestMethod.GET])
    fun callVoip(@PathVariable voipId: String): ResponseEntity<CommonResult> {
        logger.info { "callVoip $voipId" }
        return CommonResult.returnResult(relayService.callVoip(voipId))
    }

    companion object : KLogging()
}