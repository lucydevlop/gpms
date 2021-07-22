package io.glnt.gpms.common.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

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

    @Throws(UnirestException::class, JsonProcessingException::class)
    fun sendPatchRequest(url: String?, `object`: Any?): HttpResponse<JsonNode?>? {
        val objectMapper = ObjectMapper()
        val jsonInString = objectMapper.writeValueAsString(`object`)
        println(jsonInString)
        return Unirest.patch(url)
            .header("Content-Type", "application/json")
            .body(jsonInString)
            .asJson()
    }

    @Throws(UnirestException::class, JsonProcessingException::class)
    fun sendFormPostRequest(url: String?, `object`: Any?): HttpResponse<JsonNode?>? {
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

    @Throws(UnirestException::class)
    fun sendResetGetRequest(url: String?): HttpResponse<String?>? {
        return Unirest.get(url)
            .header("Authorization", "Basic YWRtaW46Z2xudDExISE=")
            .asString()
    }

    //  TODO getMeesageWithToken
    fun sendGetRequestWithToken(url: String?, token:String?): HttpResponse<JsonNode>?{
        return Unirest.get(url)
            .header("Authorization", "Bearer $token")
            .asJson()
    }


    //  TODO getMeesageWithToken
    fun sendPostRequestWithToken(url: String?, token: String?, `object`: Any?): HttpResponse<JsonNode>?{
        val objectMapper = ObjectMapper()
        val jsonInString = objectMapper.writeValueAsString(`object`)
        println(jsonInString)

        return Unirest.post(url)
            .header("Content-Type", "application/json")
            .header("Authorization", "Bearer $token")
            .body(jsonInString)
            .asJson()
    }

}