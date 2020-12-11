package io.glnt.gpms.handler.product.controller

import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.product.model.reqSearchProduct
import mu.KLogging
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}/parkinglot/product"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class ProductController {
    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun getParkinglotProduct(@RequestBody request: reqSearchProduct) {
        logger.debug("parkinglot product list = $request")
//        val result =
    }

    companion object : KLogging()
}