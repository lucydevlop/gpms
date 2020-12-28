package io.glnt.gpms.handler.user.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.user.service.AuthService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}/users"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class UserController {

    @Autowired
    private lateinit var authService: AuthService

    @RequestMapping(value = ["/{userId}"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun getUser(@PathVariable("userId") userId: String) : ResponseEntity<CommonResult> {
        logger.debug("searchUser id : $userId")
        val result = authService.getUser(userId)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    companion object : KLogging()
}