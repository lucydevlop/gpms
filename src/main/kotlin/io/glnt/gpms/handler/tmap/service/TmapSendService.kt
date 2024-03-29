package io.glnt.gpms.handler.tmap.service

import com.alibaba.fastjson.JSON
import com.mashape.unirest.http.Unirest
import io.github.jhipster.config.JHipsterDefaults.Cache.Hazelcast.ManagementCenter.url
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.handler.facility.model.reqPaymentResult
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.model.FacilitiesFailureAlarm
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.json.JSONObject
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File
import javax.validation.constraints.NotBlank


@Service
class TmapSendService (
    private val parkSiteInfoService: ParkSiteInfoService
){
    companion object : KLogging()

    @Value("\${tmap.url}")
    lateinit var url: String

    @Autowired
    lateinit var enviroment: Environment

    @Autowired
    private lateinit var restAPIManager: RestAPIManagerUtil

    fun setTmapRequest(type: String, requestId: String?, contents: Any) : reqApiTmapIF {
        return reqApiTmapIF(
            eventType = type,
            eventData = reqApiTmapCommon(
                type = type,
                parkingSiteId = parkSiteInfoService.getParkSiteId()!!,
                requestId = requestId?.let { requestId },
                eventDateTime = DateUtil.stringToNowDateTime(),
                contents = contents
            )
        )
    }

//    fun setTmapResponse(apiTmapCommon: apiTmapCommon) : apiTmapCommon {
//        return apiTmapCommon(
//            type = type,
//            parkingSiteId = parkinglotService.parkSiteId()!!,
//            requestId = requestId,
//            eventDateTime = DateUtil.stringToNowDateTime(),
//            contents = contents
//        )
//    }

    fun sendFacilitiesRegist(request: reqFacilitiesRegist, requestId: String, filePath: String)  = with(request) {
        restAPIManager.sendPostRequest(
            "$url/patient/getPatientInfo",
            setTmapRequest("facilitiesRegistRequest", requestId, request)
        )

        sendEventFileAsync(reqTmapFileUpload(
            type = "fileUpload", eventType = "facilitiesRegistRequest",
            requestId = requestId, fileUploadId = fileUploadId,
            fileName = "test.json", fileUploadDateTime = DateUtil.stringToNowDateTime()
        ), filePath)
    }

    fun sendInVehicle(request: reqTmapInVehicle, requestId: String, fileName: String?) = with(request) {
        logger.info { "sendInVehicle request $request" }
        try {
            val data = reqTmapInVehicle(
                gateId = gateId,
                sessionId = DataCheckUtil.generateSessionId("S"),
                inVehicleType = inVehicleType,
                vehicleNumber = vehicleNumber,
                recognitionType = recognitionType,
                recognitionResult = recognitionResult,
                fileUploadId = fileUploadId
            )
//            val httpResponse: HttpResponse<JsonNode?>? =
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("inVehicle", requestId, data)
            )

            val fileUpload = reqTmapFileUpload(
                type = "fileUpload",
                parkingSiteId = parkSiteInfoService.getParkSiteId()!!,
                eventType = "inVehicle",
                requestId = requestId,
                fileUploadId = fileUploadId,
                fileName = fileName!!,
                fileUploadDateTime = DateUtil.stringToNowDateTime()
            )

            restAPIManager.sendPostRequest(
                "$url/patient/getPatientInfo",
                fileUpload
            )

        } catch (e: RuntimeException) {
            logger.error { "sendInVehicle error ${e.message}" }
        }
    }

    fun sendInVehicleRequest(request: reqTmapInVehicle, requestId: String, fileName: String?) = with(request) {
        logger.info { "sendInVehicle request $request" }
        try {
//            val data = reqTmapInVehicle(
//                gateId = gateId,
////                sessionId = DataCheckUtil.generateSessionId("S"),
////                inVehicleType = inVehicleType,
//                vehicleNumber = vehicleNumber,
//                recognitionType = recognitionType,
//                recognitorResult = recognitorResult,
//                fileUploadId = fileUploadId
//            )
//            val httpResponse: HttpResponse<JsonNode?>? =
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("inVehicleRequest", requestId, request)
            )

            val fileUpload = reqTmapFileUpload(
                type = "fileUpload",
                parkingSiteId = parkSiteInfoService.getParkSiteId()!!,
                eventType = "inVehicle",
                requestId = requestId,
                fileUploadId = fileUploadId,
                fileName = fileName!!,
                fileUploadDateTime = DateUtil.stringToNowDateTime()
            )

            restAPIManager.sendPostRequest(
                "$url/patient/getPatientInfo",
                fileUpload
            )

        } catch (e: RuntimeException) {
            logger.error { "sendInVehicle error ${e.message}" }
        }
    }

    fun sendOutVehicle(request: reqOutVehicle, requestId: String, fileName: String?) = with(request) {
        logger.info { "sendInVehicle request $request" }
        try {
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("outVehicle", requestId, request)
            )

            val fileUpload = reqTmapFileUpload(
                type = "fileUpload",
                parkingSiteId = parkSiteInfoService.getParkSiteId()!!,
                eventType = "inVehicle",
                requestId = requestId,
                fileUploadId = fileUploadId,
                fileName = DataCheckUtil.getFileName(fileName!!),
                fileUploadDateTime = DateUtil.stringToNowDateTime()
            )
            sendFileUpload(fileUpload, fileName)

        } catch (e: RuntimeException) {
            logger.error { "sendOutVehicle error ${e.message}" }
        }
    }

    fun sendAdjustmentRequest(request: reqAdjustmentRequest, requestId: String) = with(request) {
        logger.info { "sendAdjustmentRequest request $request" }
        try {
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("adjustmentRequest", requestId, request)
            )

        }catch (e: RuntimeException) {
            logger.error { "sendAdjustmentRequest error ${e.message}" }
        }
    }

    fun sendFileUpload(request: reqTmapFileUpload, filePath: String?) = with(request) {
        logger.info { "sendFileUpload request $request fileName $filePath" }
        parkingSiteId = parkSiteInfoService.getParkSiteId()!!
        try {
            restAPIManager.sendFormPostRequest(url, request)
        } catch (e: RuntimeException) {
            logger.error { "sendFileUpload error ${e.message}" }
        }
    }

    fun sendFailureAlarm(request: reqTmapFailureAlarm) = with(request) {
        restAPIManager.sendPostRequest(
            "$url/patient/getPatientInfo",
            setTmapRequest("failureAlarm", null, request)
        )
    }

    fun sendEventFileAsync(data: Any, filePath: String) {
        try {
            val j = JSONObject(JSON.toJSON(data))
            val jsonResponse = Unirest.post(url)
//                .basicAuth("key", "password")
                .header("Content-Type", "multipart/form-data")
//                .header("accept", "application/json")
                .field("eventType", "facilitiesRegistRequest")
                .field("eventData", j.toString().replace("/r/n", ""))
                .field("file", File(filePath))
                .asJson()
            logger.debug { "sendEventFileAsync request : $jsonResponse" }
            logger.debug { "sendEventFileAsync response : ${jsonResponse.status}" }
            Unirest.shutdown()
        } catch (e: java.lang.RuntimeException) {

        }
    }

    fun sendHealthCheckRequest(request: reqRelayHealthCheck, requestId: String) {
        logger.info { "sendHealthCheckRequest request $request" }
        try {
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("facilitiesHealthCheckNoti", requestId, request)
            )

        }catch (e: RuntimeException) {
            logger.error { "sendAdjustmentRequest error ${e.message}" }
        }
    }

    fun sendFacilitiesFailureAlarm(request: FacilitiesFailureAlarm, requestId: String?) {
        logger.info { "sendFacilitiesFailureAlarm request $request" }
        try {
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("failureAlarm", requestId, request)
            )

        }catch (e: RuntimeException) {
            logger.error { "sendAdjustmentRequest error ${e.message}" }
        }
    }

    fun sendFacilitiesStatusNoti(request: reqTmapFacilitiesStatusNoti, requestId: String?) {
        logger.info { "sendFacilitiesStatusNoti request $request" }
        try {
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("facilitiesStatusNoti", requestId, request)
            )

        }catch (e: RuntimeException) {
            logger.error { "sendAdjustmentRequest error ${e.message}" }
        }
    }

    fun sendPayment(request: reqSendPayment, requestId: String) {
        logger.info { "sendPayment request $request" }
        try {
            restAPIManager.sendPostRequest(
                url,
                setTmapRequest("payment", requestId, request)
            )
        }catch (e: RuntimeException) {
            logger.error { "sendPayment error ${e.message}" }
        }
    }

    fun sendTmapInterface(data: Any, requestId: String, type: String) {
        restAPIManager.sendPostRequest(
            url,
            setTmapRequest(type, requestId, data)
        )
    }
}