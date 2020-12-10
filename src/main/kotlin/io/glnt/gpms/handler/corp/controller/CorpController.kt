package io.glnt.gpms.io.glnt.gpms.handler.corp.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.parkinglot.controller.ParkinglotController
import io.glnt.gpms.io.glnt.gpms.handler.corp.model.reqSearchCorp
import io.glnt.gpms.io.glnt.gpms.handler.corp.service.CorpService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}/corp"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CorpController {

    @Autowired
    private lateinit var corpService: CorpService

    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getCorp(@RequestBody request: reqSearchCorp) : ResponseEntity<CommonResult> {
        logger.debug("list corp = $request")
        val result = corpService.getCorp(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }


    companion object : KLogging()


}