package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.user.controller.AuthController
import io.glnt.gpms.handler.user.model.reqLogin
import io.glnt.gpms.handler.user.model.reqRegister
import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.service.SiteUserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class UserResource(
    private val siteUserService: SiteUserService,
    private val authService: AuthService,
    private val passwordEncoder: PasswordEncoder
) {
    @RequestMapping(value = ["/login/admin"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun adminLogin(@RequestBody request: reqLogin) : ResponseEntity<CommonResult> {
        AuthController.logger.debug("admin login = $request")
        val result = authService.adminLogin(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/admin/users"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun adminRegister(@RequestBody request: reqRegister) : ResponseEntity<CommonResult> {
        AuthController.logger.debug("admin register = $request")
        val result = authService.adminRegister(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            ResultCode.CONFLICT.getCode() -> ResponseEntity(result, HttpStatus.CONFLICT)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }


}