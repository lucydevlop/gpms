package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.RelayClient
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.external.service.externalService
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.ArrayList

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ExternalResource(
    private var externalService: externalService,
    private var relayClient: RelayClient
) {
    companion object : KLogging()

    @RequestMapping(value = ["/external/gate/{gateId}/{action}"], method = [RequestMethod.GET])
    fun gateAction(@RequestParam gateId: String, @RequestParam action: String) : ResponseEntity<CommonResult> {
        logger.debug("gateAction $gateId, $action")
        relayClient.sendActionBreaker(gateId, action, "manual")
        return CommonResult.returnResult(CommonResult.data())
    }

    @RequestMapping(value = ["/external/tickets"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getTickets(@RequestBody request: reqSearchProductTicket) : ResponseEntity<CommonResult> {
        logger.debug("getTickets")
        return CommonResult.returnResult(externalService.searchTickets(request))
    }

    @RequestMapping(value = ["/external/tickets"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createTickets(@RequestBody request: ArrayList<reqCreateProductTicket>) : ResponseEntity<CommonResult> {
        logger.debug{ "createTickets $request" }
        return CommonResult.returnResult(externalService.createTickets(request))
    }

    @RequestMapping(value = ["/external/tickets"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteTickets(@RequestBody request: ArrayList<reqCreateProductTicket>) : ResponseEntity<CommonResult> {
        logger.debug{ "deleteTickets $request" }
        return CommonResult.returnResult(externalService.deleteTickets(request))
    }
}