package io.glnt.gpms.io.glnt.gpms.handler.discount.controller

import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping(path=["/$API_VERSION/discount"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class DiscountController {


}