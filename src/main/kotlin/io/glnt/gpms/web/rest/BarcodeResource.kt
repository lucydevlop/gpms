package io.glnt.gpms.io.glnt.gpms.web.rest

import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.service.BarcodeService
import mu.KLogging
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class BarcodeResource (
    private val barcodeService: BarcodeService
){
    companion object : KLogging()
}