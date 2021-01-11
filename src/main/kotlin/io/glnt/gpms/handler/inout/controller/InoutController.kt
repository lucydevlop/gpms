package io.glnt.gpms.handler.inout.controller

import io.glnt.gpms.common.api.*
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.inout.model.reqAddParkIn
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.common.api.utils.paginate
import io.glnt.gpms.handler.inout.model.ResParkInList
import io.glnt.gpms.handler.inout.model.reqAddParkOut
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/inout"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class InoutController {

    @Autowired
    private lateinit var inoutService: InoutService

    @RequestMapping(value = ["/parkin"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun parkIn(@RequestBody request: reqAddParkIn) : ResponseEntity<CommonResult> {
        val result = inoutService.parkIn(request)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/parkout"], method = [RequestMethod.POST])
//    @ResponseStatus(CREATED)
    @Throws(CustomException::class)
    fun parkOut(@RequestBody request: reqAddParkOut) : ResponseEntity<CommonResult> {
        val result = inoutService.parkOut(request)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getAllParkInLists(@RequestBody request: reqSearchParkin) : ResponseEntity<PaginationResult<ResParkInList>> {
        return when (val result = inoutService.getAllParkLists(request)) {
            null -> ResponseEntity.ok(emptyPaginationResult())
            else ->  {
                ResponseEntity.ok(result.paginate(localPagination(request.page!!, request.pageSize!!.toInt())))
            }
        }
    }

    companion object : KLogging()
}