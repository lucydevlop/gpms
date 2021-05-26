package io.glnt.gpms.handler.rcs.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.enums.ExternalSvrType
import io.glnt.gpms.handler.rcs.model.*
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.enums.checkUseStatus
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class RcsService(
    private var facilityService: FacilityService,
    private var parkinglotService: ParkinglotService,
    private var inoutService: InoutService,
    private var restAPIManager: RestAPIManagerUtil,
    private var relayService: RelayService
) {
    companion object : KLogging()

    @Value("\${glnt.url}")
    lateinit var glntUrl: String

    @Value("\${adtcaps.url}")
    lateinit var adtcapsUrl: String

    fun asyncFacilities(): CommonResult {
        return CommonResult.data(facilityService.activeGateFacilities())
    }

    fun asyncFailureAlarm(request: Failure) {
        try {
            logger.info { "Async Failure Alarm $request"  }
            when (parkinglotService.parkSite!!.externalSvr) {
                ExternalSvrType.GLNT -> {
                    restAPIManager.sendPostRequest(
                        glntUrl+"/parkinglots/facilities/errors",
                        ReqFailureAlarm(parkinglotId = 1, facilityId = request.facilitiesId!!, createDate = request.issueDateTime.toString(), contents = request.failureCode!!)
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
                        ReqFailureAlarm(parkinglotId = 1, facilityId = request.facilitiesId!!, createDate = request.expireDateTime.toString(), contents = request.failureCode!!, resolvedYn = checkUseStatus.Y)
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
                        glntUrl+"/parkinglots/1/facilities",
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
                        glntUrl+"/parkinglots/1/facilities",
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
                "UPLOCK" -> "unLock"
                else -> status
            }
            when(status) {
                "UP", "DOWN", "UPLOCK" -> relayService.actionGate(facilityId, "FACILITI", action)
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
}