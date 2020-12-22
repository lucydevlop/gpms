package io.glnt.gpms.handler.auth.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.auth.model.reqLogin
import io.glnt.gpms.handler.auth.model.reqRegister
import io.glnt.gpms.handler.auth.model.reqUserRegister
import io.glnt.gpms.handler.corp.model.reqSearchCorp
import io.glnt.gpms.handler.auth.service.AuthService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}/auth"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class AuthController {

    @Autowired
    private lateinit var authService: AuthService

    @RequestMapping(value = ["/admin/login"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun adminLogin(@RequestBody request: reqLogin) : ResponseEntity<CommonResult> {
        logger.debug("admin login = $request")
        val result = authService.adminLogin(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/admin/register"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun adminRegister(@RequestBody request: reqRegister) : ResponseEntity<CommonResult> {
        logger.debug("admin register = $request")
        val result = authService.adminRegister(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.CONFLICT.getCode() -> ResponseEntity(result, HttpStatus.CONFLICT)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/user/login"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun userLogin(@RequestBody request: reqLogin) : ResponseEntity<CommonResult> {
        logger.debug("store login = $request")
        val result = authService.userLogin(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/user/register"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun userRegister(@RequestBody request: reqUserRegister) : ResponseEntity<CommonResult> {
        logger.debug("generate store Id")
        val result = authService.userRegister(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

//    @RequestMapping(value = ["/user/id"], method = [RequestMethod.POST])
//    @Throws(CustomException::class)
//    fun generateUserId() : ResponseEntity<CommonResult> {
//        logger.debug("generate store Id")
//        val result = authService.generateUserId()
//
//        return when(result.code) {
//            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
//            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
//            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
//            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
//
//        }
//    }

    companion object : KLogging()
}