package io.glnt.gpms.io.glnt.gpms.handler.discount.controller

import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.discount.service.DiscountService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path=["/$API_VERSION/ticket"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DiscountController {

    @Autowired
    private lateinit var discountService: DiscountService

//    @RequestMapping(value = ["/list"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)

    companion object : KLogging()
}