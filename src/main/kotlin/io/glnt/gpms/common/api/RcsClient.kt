package io.glnt.gpms.common.api

import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.handler.rcs.model.ReqFacilityStatus
import io.glnt.gpms.handler.rcs.model.ReqFailureAlarm
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.enums.ExternalSvrType
import io.glnt.gpms.model.enums.checkUseStatus
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RcsClient(
    private var restAPIManager: RestAPIManagerUtil
) {
    companion object : KLogging()

    @Value("\${glnt.url}")
    lateinit var glntUrl: String

    @Value("\${adtcaps.url}")
    lateinit var adtcapsUrl: String

    fun asyncFacilitiesStatus(reqeust: List<ReqFacilityStatus>, externalSvr: ExternalSvrType, rcsParkId: Long) {
        when (externalSvr) {
            ExternalSvrType.GLNT -> {
                restAPIManager.sendPatchRequest("$glntUrl/parkinglots/$rcsParkId/facilities",
                    reqeust
                )
            }
        }
    }

    fun asyncRestoreAlarm(request: Failure, externalSvr: ExternalSvrType, rcsParkId: Long) {
        when (externalSvr) {
            ExternalSvrType.GLNT -> {
                restAPIManager.sendPatchRequest("$glntUrl/parkinglots/facilities/errors",
                    ReqFailureAlarm(parkinglotId = rcsParkId, facilityId = request.facilitiesId!!, createDate = request.expireDateTime.toString(), contents = request.failureCode!!, resolvedYn = checkUseStatus.Y)
                )
            }
        }
    }

    fun asyncFailureAlarm(request: Failure, externalSvr: ExternalSvrType, rcsParkId: Long) {
        when (externalSvr) {
            ExternalSvrType.GLNT -> {
                restAPIManager.sendPatchRequest("$glntUrl/parkinglots/facilities/errors",
                    ReqFailureAlarm(parkinglotId = rcsParkId, facilityId = request.facilitiesId!!, createDate = request.issueDateTime.toString(), contents = request.failureCode!!)
                )
            }
        }
    }
}