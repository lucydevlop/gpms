package io.glnt.gpms.handler.vehicle.controller

import io.glnt.gpms.common.api.*
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.vehicle.service.VehicleService
import io.glnt.gpms.handler.vehicle.model.reqAddParkIn
import io.glnt.gpms.handler.vehicle.model.reqSearchParkin
import io.glnt.gpms.common.api.utils.paginate
import io.glnt.gpms.handler.vehicle.model.ResParkInList
import io.netty.handler.codec.http.HttpResponseStatus.CREATED
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/vehicle"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class VehicleController {

    @Autowired
    private lateinit var vehicleService: VehicleService

    @RequestMapping(value = ["/parkin"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun parkIn(@RequestBody request: reqAddParkIn) : ResponseEntity<CommonResult> {
        val result = vehicleService.parkIn(request)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/parkout"], method = [RequestMethod.POST])
//    @ResponseStatus(CREATED)
    @Throws(CustomException::class)
    fun parkOut(@RequestBody request: reqAddParkIn) : ResponseEntity<CommonResult> {
        val result = vehicleService.parkIn(request)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/parkin/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getAllParkInLists(@RequestBody request: reqSearchParkin) : ResponseEntity<PaginationResult<ResParkInList>> {
        return when (val result = vehicleService.getAllParkLists(request)) {
            null -> ResponseEntity.ok(emptyPaginationResult())
            else ->  {
                ResponseEntity.ok(result.paginate(localPagination(request.page!!, request.pageSize!!.toInt())))
            }
        }
    }

    companion object : KLogging()
}