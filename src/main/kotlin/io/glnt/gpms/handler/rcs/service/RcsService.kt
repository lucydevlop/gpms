package io.glnt.gpms.handler.rcs.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.service.singleTimer
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.rcs.model.*
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ExternalSvrType
import io.glnt.gpms.model.enums.checkUseStatus
import io.reactivex.Observable
import mu.KLogging
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

@Service
class RcsService(
    private var facilityService: FacilityService,
    private var parkinglotService: ParkinglotService,
    private var inoutService: InoutService,
    private var restAPIManager: RestAPIManagerUtil,
    private var relayService: RelayService,
    private var productService: ProductService
) {
    companion object : KLogging()

    @Value("\${glnt.url}")
    lateinit var glntUrl: String

    @Value("\${adtcaps.url}")
    lateinit var adtcapsUrl: String

    fun asyncParkinglot(): ResAsyncParkinglot? {
        try {
            when (parkinglotService.parkSite!!.externalSvr) {
                ExternalSvrType.GLNT -> {
                    val response: HttpResponse<JsonNode?>? = restAPIManager.sendPostRequest(
                        glntUrl+"/parkinglots",
                        ReqParkinglot(
                            parkinglot = AsyncParkinglot(ip = parkinglotService.parkSite!!.ip!!, name = parkinglotService.parkSite!!.sitename, city = parkinglotService.parkSite!!.city!!, address = parkinglotService.parkSite!!.address!! ),
                            facilities = facilityService.allFacilities()!!)
                    )
                    response?.let { response ->
                        if (response.status == 200 || response.status == 201) {
                            val obj = response.body!!.`object`
                            val contents = jacksonObjectMapper().readValue(obj.toString(), ResAsyncParkinglot::class.java) as ResAsyncParkinglot
                            return contents
                        }
                    }
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "RCS Async Parkinglot failed $e" }
        }
        return null
    }

    fun asyncFacilities(): CommonResult {
        return CommonResult.data(facilityService.allFacilities())
    }

    fun asyncFailureAlarm(request: Failure) {
        try {
            logger.info { "Async Failure Alarm $request"  }
            when (parkinglotService.parkSite!!.externalSvr) {
                ExternalSvrType.GLNT -> {
                    restAPIManager.sendPostRequest(
                        glntUrl+"/parkinglots/facilities/errors",
                        ReqFailureAlarm(parkinglotId = parkinglotService.parkSite!!.rcsParkId!!, facilityId = request.facilitiesId!!, createDate = request.issueDateTime.toString(), contents = request.failureCode!!)
                    )
                }
            }

        }catch (e: RuntimeException) {
            logger.error { "RCS Async Failure Alarm $request $e" }
        }
    }

    fun asyncRestoreAlarm(request: Failure) {
        try {
            logger.info { "Async Restore Alarm $request"  }
            when (parkinglotService.parkSite!!.externalSvr) {
                ExternalSvrType.GLNT -> {
                    restAPIManager.sendPatchRequest(
                        glntUrl+"/parkinglots/facilities/errors",
                        ReqFailureAlarm(parkinglotId = parkinglotService.parkSite!!.rcsParkId!!, facilityId = request.facilitiesId!!, createDate = request.expireDateTime.toString(), contents = request.failureCode!!, resolvedYn = checkUseStatus.Y)
                    )
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "RCS Async Restore Alarm $request $e" }
        }

    }

    fun asyncFacilitiesHealth(request: List<ResAsyncFacility>) {
        try {
            logger.info { "Async facilities health $request"  }
            var result = ArrayList<ReqHealthCheck>()
            request.forEach { it ->
                result.add(ReqHealthCheck(
                    dtFacilitiesId = it.dtFacilitiesId,
                    health = it.health,
                    healthDateTime = it.healthDate?.let { DateUtil.formatDateTime(it) }
                ))
            }

            when (parkinglotService.parkSite!!.externalSvr) {
                ExternalSvrType.GLNT -> {
                    restAPIManager.sendPatchRequest(
                        glntUrl+"/parkinglots/"+parkinglotService.parkSite!!.rcsParkId!!+"/facilities",
                        result
                    )
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "RCS Async facilities health $request $e" }
        }
    }

    fun asyncFacilitiesStatus(request: List<ResAsyncFacility>) {
        try {
            logger.info { "Async facilities status $request"  }
            var result = ArrayList<ReqFacilityStatus>()
            request.forEach { it ->
                if (it.category == "BREAKER")
                    result.add(ReqFacilityStatus(
                        dtFacilitiesId = it.dtFacilitiesId,
                        status = it.status,
                        statusDateTime = it.statusDate?.let { DateUtil.formatDateTime(it) }
                    ))
            }

            when (parkinglotService.parkSite!!.externalSvr) {
                ExternalSvrType.GLNT -> {
                    restAPIManager.sendPatchRequest(
                        glntUrl+"/parkinglots/"+parkinglotService.parkSite!!.rcsParkId!!+"/facilities",
                        result
                    )
                }
            }
        }catch (e: RuntimeException) {
            logger.error { "RCS Async facilities status $request $e" }
        }
    }

    fun facilityAction(facilityId: String, status: String): CommonResult {
        try {
            var action = when(status) {
                "UP" -> "open"
                "DOWN" -> "close"
                "UPLOCK" -> "uplock"
                "UNLOCK" -> "unlock"
                else -> status
            }
            when(status) {
                "UP", "DOWN", "UPLOCK", "UNLOCK" -> relayService.actionGate(facilityId, "FACILITI", action)
                "RESET" -> {
                    parkinglotService.getFacilityByDtFacilityId(facilityId)?.let { facility ->
                        facility.resetPort?.let { it ->
                            var port = it.toInt()-1
                            if (port < 0) return CommonResult.error("Reset Action failed")
                            parkinglotService.getGate(facility.gateId)?.let { gate ->
                                val url = gate.resetSvr+port
                                restAPIManager.sendResetGetRequest(url).let { response ->
                                    singleTimer()
                                    logger.info { "reset response ${response!!.status} ${response.body.toString()}" }
                                    if (response!!.status == HttpStatus.SC_OK) {
                                        Observable.timer(2, TimeUnit.SECONDS).subscribe {
                                            logger.info { "reset one more ${url}" }
                                            restAPIManager.sendResetGetRequest(url).let { reResponse ->
                                                logger.info { "reset re response ${reResponse!!.status} ${response.body.toString()}" }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                }
            }
            return CommonResult.data()
        }catch (e: RuntimeException) {
            logger.error { "RCS Breaker $facilityId status $status $e" }
            return CommonResult.error("RCS Breaker $facilityId status $status failed")
        }
    }

    @Throws(CustomException::class)
    fun getInouts(request: reqSearchParkin): CommonResult {
        try {
            return CommonResult.data(inoutService.getAllParkLists(request))
        }catch (e: CustomException){
            logger.error { "rcs getInouts failed $e" }
            return CommonResult.error("Admin getInouts failed")
        }
    }

    @Throws(CustomException::class)
    fun createInout(request: resParkInList) : CommonResult {
        try {
            return CommonResult.data(inoutService.createInout(request).data)
        }catch (e: CustomException){
            logger.error { "rcs createInout failed $e" }
            return CommonResult.error("rcs createInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun calcInout(request: resParkInList) : CommonResult {
        try {
            return CommonResult.data(inoutService.calcInout(request))
        }catch (e: CustomException){
            logger.error { "rcs calcInout failed $e" }
            return CommonResult.error("rcs calcInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun updateInout(request: resParkInList) : CommonResult {
        try {
            return CommonResult.data(inoutService.updateInout(request))
        }catch (e: CustomException){
            logger.error { "rcs updateInout failed $e" }
            return CommonResult.error("rcs updateInout failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun getTickets(request: reqSearchProductTicket): CommonResult {
        try {
//            productService.getProducts(request)?.let { tickets ->
//                tickets.forEach {
//
//                }
//            }
            return CommonResult.data(productService.getProducts(request))
        }catch (e: CustomException) {
            logger.error { "rcs getTickets failed $e" }
            return CommonResult.error("rcs getTickets failed")
        }
    }

    @Throws(CustomException::class)
    fun createTicket(request: ProductTicket) : CommonResult {
        try {
            request.delYn = DelYn.N
            return CommonResult.data(productService.saveProductTicket(request))
        }catch (e: CustomException) {
            logger.error { "rcs createTicket failed $e" }
            return CommonResult.error("rcs createTicket failed")
        }
    }

    private fun responseToMap(response: HttpResponse<JsonNode?>): MutableMap<String, Any>
        = response.body!!.`object`.toMap()

}