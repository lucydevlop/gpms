package io.glnt.gpms.handler.user.controller

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.user.model.reqLogin
import io.glnt.gpms.handler.user.model.reqRegister
import io.glnt.gpms.handler.user.model.reqUserRegister
import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.model.dto.ChangePasswordDTO
import io.glnt.gpms.model.dto.entity.SiteUserDTO
import io.glnt.gpms.service.SiteUserService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping(path=["/${ApiConfig.API_VERSION}/auth"])
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class AuthController(
    private val siteUserService: SiteUserService
) {

    @Autowired
    private lateinit var authService: AuthService

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

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
        val result = authService.userRegister(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/user/registers"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun userRegisters(@RequestBody request: Array<reqUserRegister>) : ResponseEntity<CommonResult> {
        val result = authService.userRegisters(request)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
        }
    }

    @RequestMapping(value = ["/tokens"], method = [RequestMethod.GET])
    @Throws(CustomException::class)
    fun token(servlet: HttpServletRequest) : ResponseEntity<CommonResult> {
        logger.debug("get token")
        val result = authService.getToken(servlet)

        return when(result.code) {
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            ResultCode.UNAUTHORIZED.getCode() -> ResponseEntity(result, HttpStatus.UNAUTHORIZED)
            ResultCode.UNPROCESSABLE_ENTITY.getCode() -> ResponseEntity(result, HttpStatus.UNPROCESSABLE_ENTITY)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)

        }
    }

    @RequestMapping(value = ["/change/password"], method = [RequestMethod.POST])
    @Throws(CustomException::class)
    fun changePassword(@RequestBody request: ChangePasswordDTO): ResponseEntity<CommonResult> {
        logger.debug { "change password ${request.idx} " }
        if (request.idx == null) {
            throw CustomException(
                "change password not found idx",
                ResultCode.FAILED
            )
        }

        siteUserService.findByIdx(request.idx?: -1).orElse(null)?.let { user ->
            if (!isPasswordMatches(user, request.password?: "")) {
                throw CustomException(
                    "password is not match",
                    ResultCode.FAILED
                )
            }
            user.passwordDate = LocalDateTime.now()
            user.password = passwordEncoder.encode(request.newPassword)
            return CommonResult.returnResult(CommonResult.data(siteUserService.save(user)))
        }?: run {
            throw CustomException(
                "change password user not found idx ${request.idx}",
                ResultCode.FAILED
            )
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

    private fun isPasswordMatches(siteUser: SiteUserDTO, currentPassword: String): Boolean {
        return passwordEncoder.matches(currentPassword, siteUser.password)
    }

    companion object : KLogging()
}