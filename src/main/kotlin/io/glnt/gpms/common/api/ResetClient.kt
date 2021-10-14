package io.glnt.gpms.common.api

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.Unirest
import com.mashape.unirest.http.exceptions.UnirestException
import mu.KLogging
import org.springframework.stereotype.Component

@Component
class ResetClient {
    companion object : KLogging()

    @Throws(UnirestException::class)
    fun sendReset(url: String?): HttpResponse<String?>? {
        return Unirest.get(url)
            .header("Authorization", "Basic YWRtaW46Z2xudDExISE=")
            .asString()
    }

}