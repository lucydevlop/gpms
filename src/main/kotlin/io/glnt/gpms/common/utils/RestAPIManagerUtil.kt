package io.glnt.gpms.common.utils

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONObject
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okio.ByteString
import org.springframework.stereotype.Component
import java.io.InputStream
import java.util.concurrent.TimeUnit

@Component
class RestAPIManagerUtil {
    private val UNIREST_STRING_ACCEPT = "accept"
    private val UNIREST_STRING_JSON = "application/json"
    private val UNIREST_STRING_CONTENT_TYPE = "Content-Type"

    @Throws(UnirestException::class)
    fun sendGetRequest(url: String?): HttpResponse<JsonNode?>? {
        return Unirest.get(url)
            .asJson()
    }

    @Throws(UnirestException::class, JsonProcessingException::class)
    fun sendPostRequest(url: String?, `object`: Any?): HttpResponse<JsonNode?>? {
        val objectMapper = ObjectMapper()
        val jsonInString = objectMapper.writeValueAsString(`object`)
        println(jsonInString)
        return Unirest.post(url)
            .header("Content-Type", "application/json")
            .body(jsonInString)
            .asJson()
    }

//    @Throws(UnirestException::class, JsonProcessingException::class)
//    fun sendPostRequest(url: String?, `object`: Any?, ): HttpResponse<JsonNode?>? {
//        val objectMapper = ObjectMapper()
//        val jsonInString = objectMapper.writeValueAsString(`object`)
//        println(jsonInString)
//        return Unirest.post(url)
//            .header("Content-Type", "application/json")
//            .body(jsonInString)
//            .asJson()
//    }

    @Throws(UnirestException::class, JsonProcessingException::class)
    fun sendPutRequest(url: String?, `object`: Any?): HttpResponse<JsonNode?>? {
        val objectMapper = ObjectMapper()
        val jsonInString = objectMapper.writeValueAsString(`object`)
        println(jsonInString)
        return Unirest.post(url)
            .header("Content-Type", "application/json")
            .body(jsonInString)
            .asJson()
    }

    @Throws(UnirestException::class, JsonProcessingException::class)
    fun sendDeleteRequest(url: String?, `object`: Any?): HttpResponse<JsonNode?>? {
        val objectMapper = ObjectMapper()
        val jsonInString = objectMapper.writeValueAsString(`object`)
        return Unirest.delete(url)
            .header("Content-Type", "application/json")
            .body(jsonInString)
            .asJson()
    }
}