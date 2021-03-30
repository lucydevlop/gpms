package io.glnt.gpms.handler.product.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.product.model.reqCreateProduct
import io.glnt.gpms.handler.product.model.reqSearchProduct
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.model.entity.ProductTicket
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/product/ticket"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ProductController {

    @Autowired
    private lateinit var productService: ProductService

//    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun getParkinglotProduct(@RequestBody request: reqSearchProduct) : ResponseEntity<CommonResult> {
//        logger.trace("parkinglot product list = $request")
//        val result = productService.getProducts(request)
//        return when(result.code) {
//            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//        }
//    }

//    @RequestMapping(value = ["/create"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun createTicket(@RequestBody request: reqCreateProduct) : ResponseEntity<CommonResult> {
//        logger.trace("parkinglot product create : $request")
//        val result = productService.createProduct(request)
//        return when(result) {
//            true -> ResponseEntity(CommonResult.created(), HttpStatus.OK)
//            else -> ResponseEntity(CommonResult.error("createTicket failed"), HttpStatus.BAD_REQUEST)
//
//        }
//    }

    @RequestMapping(value = ["/delete/{request}"], method = [RequestMethod.DELETE])
    @Throws(CustomException::class)
    fun deleteTicket(@PathVariable request: Long): ResponseEntity<CommonResult> {
        logger.trace("ticket delete id = $request")
        val result = productService.deleteTicket(request)
        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    companion object : KLogging()
}