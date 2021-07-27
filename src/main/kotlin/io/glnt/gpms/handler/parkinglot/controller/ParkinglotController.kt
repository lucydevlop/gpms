package io.glnt.gpms.handler.parkinglot.controller

import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.parkinglot.model.reqCreateParkinglot
import io.glnt.gpms.handler.parkinglot.model.reqUpdateGates
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.service.BarcodeClassService
import io.glnt.gpms.service.BarcodeService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping(
    path = ["/${API_VERSION}/parkinglot"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ParkinglotController {

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var barcodeClassService: BarcodeClassService

    @Autowired
    private lateinit var barcodeService: BarcodeService

    @RequestMapping(value= ["/create"], method = [RequestMethod.POST])
    fun createParkinglot() {
        logger.debug("parkinglot createParkinglot  = ")
        parkinglotService.createParkinglot()

    }

    @RequestMapping(method = [RequestMethod.GET])
    fun getParkinglot() : ResponseEntity<CommonResult> {
        logger.debug { "parkinglot search" }
        val result = parkinglotService.getParkinglot()
        return when (result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value= ["/update"], method = [RequestMethod.POST])
    fun updateParkinglot(@RequestBody request: reqCreateParkinglot) : ResponseEntity<CommonResult> {
        logger.info { "updateParkinglot request $request" }
        val result = parkinglotService.updateParkinglot(request)
        return when (result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }

    }

    @RequestMapping(value = ["/facility/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkinglotfacilities(@RequestBody request: reqSearchParkinglotFeature): ResponseEntity<CommonResult> {
        logger.debug("parkinglot facility list  = $request")
        val result = parkinglotService.getParkinglotfacilities(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/gate/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkinglotGates(@RequestBody request: reqSearchParkinglotFeature): ResponseEntity<CommonResult> {
        logger.trace("parkinglot gate list  = $request")
        val result = parkinglotService.getParkinglotGates(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/gate/update"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun updateGates(@RequestBody request: reqUpdateGates): ResponseEntity<CommonResult> {
        logger.trace("parkinglot gate update  = $request")
        val result = parkinglotService.updateGates(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/gate/create"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun createGate(@RequestBody request: Gate): ResponseEntity<CommonResult> {
        logger.trace("parkinglot gate create  = $request")
        val result = parkinglotService.createGate(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/gate/delete/{id}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteGate(@PathVariable id: Long): ResponseEntity<CommonResult> {
        logger.trace("parkinglot gate delete : $id")
        val result = parkinglotService.deleteGate(id)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }


//    @RequestMapping(value = ["/feature/list"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun getParkinglotFeature(@RequestBody request: reqSearchParkinglotFeature): ResponseEntity<CommonResult> {
//        logger.trace("parkinglot feature list  = $request")
//        val result = parkinglotService.getParkinglotFeature(request)
//
//        return when(result.code) {
//            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//
//        }
//    }
//
//    @RequestMapping(value = ["/feature/add"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun addParkinglotFeature(@RequestBody request: reqAddParkinglotFeature): ResponseEntity<CommonResult> {
//        logger.debug("list parkinglot = $request")
//        val result = parkinglotService.addParkinglotFeature(request)
//        return when(result.code){
//            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//        }
//    }

    @RequestMapping(value= ["/facility/{id}"], method = [RequestMethod.GET])
    fun getFacility(@PathVariable("id") id: String): ResponseEntity<CommonResult> {
        logger.debug("facility getFacility : $id")

        val result = parkinglotService.searchFacility(id)
        return when (result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/discount/ticket"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getDiscountCoupon() : ResponseEntity<CommonResult> {
        logger.debug { "getDiscountCoupon" }
        val discountClass = parkinglotService.getDiscountCoupon()

        return when(discountClass.code) {
            ResultCode.SUCCESS.getCode() -> {
                var result = HashMap<String, Any?>()
                result = hashMapOf(
                    "discountClass" to discountClass.data,
                    "barcodeClass" to barcodeClassService.findAll(),
                    "barcodeInfo" to barcodeService.findAll()
                )
                ResponseEntity.ok(CommonResult.data(result))
            }
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(discountClass, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(discountClass, HttpStatus.BAD_REQUEST)
        }
    }



//    @RequestMapping(value = ["/add"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun addVehicle(json: String): CommonResult<Int>? {
//        logger.debug("add vehicle = $json")
//        val result = CommonResult<Int>()
//        val rtn: Int = vehicleService.addVehicle(json)
//        result.setRespData(rtn)
//        return result
//    }

//    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun getAllParkinglot(@RequestBody request: reqSearchParkinglot): CommonResult<Objects>? {
//        logger.debug("list parkinglot = $request")
//        val result = CommonResult<Objects>()
//        return result
//
//    }

    companion object : KLogging()
}