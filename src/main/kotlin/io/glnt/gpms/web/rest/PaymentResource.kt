package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.handler.relay.controller.RelayController
import io.glnt.gpms.service.InoutPaymentService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class PaymentResource (
    private val rcsService: RcsService,
    private val inoutPaymentService: InoutPaymentService,
) {
    @PostMapping(value = ["/api/v1/upload/receipt/{sn}"], consumes = ["multipart/form-data"], produces = ["application/json"])
    fun upload(@PathVariable sn: Long, @RequestPart("file") file: MultipartFile): ResponseEntity<CommonResult> {

        inoutPaymentService.uploadReceipt(sn, file)
//        val file = fileService.save(file)
        return ResponseEntity.ok(CommonResult.data())
    }

    @RequestMapping(value = ["/api/v1/relay/call/voip/{voipId}"], method = [RequestMethod.GET])
    fun callVoip(@PathVariable voipId: String): ResponseEntity<CommonResult> {
        RelayController.logger.info { "callVoip $voipId" }
        return CommonResult.returnResult(rcsService.asyncCallVoip(voipId))
    }
}