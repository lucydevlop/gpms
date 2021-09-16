package io.glnt.gpms.handler.external.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.external.service.externalService
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/external"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ExternalController {
    @Autowired
    private lateinit var externalService: externalService

    @RequestMapping(value = ["/tickets"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getTickets(@RequestBody request: reqSearchProductTicket) : ResponseEntity<CommonResult> {
        logger.trace("getTickets")
        return CommonResult.returnResult(externalService.searchTickets(request))
    }

    @RequestMapping(value = ["/tickets"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createTickets(@RequestBody request: ArrayList<reqCreateProductTicket>) : ResponseEntity<CommonResult> {
        logger.trace{ "createTickets $request" }
        return CommonResult.returnResult(externalService.createTickets(request))
    }

    @RequestMapping(value = ["/tickets"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteTickets(@RequestBody request: ArrayList<reqCreateProductTicket>) : ResponseEntity<CommonResult> {
        logger.trace{ "deleteTickets $request" }
        return CommonResult.returnResult(externalService.deleteTickets(request))
    }

    companion object : KLogging()
}