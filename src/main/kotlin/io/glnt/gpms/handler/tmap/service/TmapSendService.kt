package io.glnt.gpms.handler.tmap.service

import com.alibaba.fastjson.JSON
import com.mashape.unirest.http.Unirest
import io.github.jhipster.config.JHipsterDefaults.Cache.Hazelcast.ManagementCenter.url
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.tmap.model.*
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
class TmapSendService {
    companion object : KLogging()

    @Value("\${tmap.url}")
    lateinit var url: String

    @Autowired
    lateinit var enviroment: Environment

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var restAPIManager: RestAPIManagerUtil

    fun setTmapRequest(type: String, requestId: String?, contents: Any) : reqApiTmapCommon {
        return reqApiTmapCommon(
            type = type,
            parkingSiteId = parkinglotService.parkSiteId()!!,
            requestId = requestId?.let { requestId },
            eventDateTime = DateUtil.stringToNowDateTime(),
            contents = contents
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
        logger.debug { "sendInVehicle request ${request}" }
        try {
            val data = reqTmapInVehicle(
                gateId = gateId,
                sessionId = DataCheckUtil.generateSessionId("S"),
                inVehicleType = inVehicleType,
                vehicleNumber = vehicleNumber,
                recognitionType = recognitionType,
                recognitorResult = recognitorResult,
                fileUploadId = fileUploadId
            )
//            val httpResponse: HttpResponse<JsonNode?>? =
            restAPIManager.sendPostRequest(
                "$url/patient/getPatientInfo",
                setTmapRequest("inVehicle", requestId, data)
            )

            val fileUpload = reqTmapFileUpload(
                type = "fileUpload",
                parkingSiteId = parkinglotService.parkSiteId()!!,
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

    fun sendFileUpload(request: reqTmapFileUpload) = with(request) {
        logger.debug { "sendFileUpload request ${request}" }
        parkingSiteId = parkinglotService.parkSiteId()!!
        try {
            restAPIManager.sendPostRequest(
                "$url/patient/getPatientInfo",
                request
            )

        } catch (e: RuntimeException) {
            logger.error { "sendInVehicle error ${e.message}" }
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

    fun sendProfileSetupResponse(data: Any, requestId: String) {
        restAPIManager.sendPostRequest(
            url,
            setTmapRequest("profileSetupResponse", requestId, data)
        )
    }
}