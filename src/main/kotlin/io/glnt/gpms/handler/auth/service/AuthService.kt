package io.glnt.gpms.handler.auth.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.auth.model.reqLogin
import io.glnt.gpms.handler.auth.model.reqRegister
import io.glnt.gpms.handler.auth.model.resLogin
import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.repository.UserRepository
import io.glnt.gpms.security.jwt.JwtTokenProvider
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service

@Service
class AuthService {
    companion object : KLogging()

    @Autowired
    private lateinit var authenticationManager: AuthenticationManager

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var userRepository: UserRepository

    fun adminLogin(request: reqLogin) : CommonResult = with(request) {
        try {
//			val token = UsernamePasswordAuthenticationToken(request.email, request.password)
//			authenticationManager.authenticate(token)
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(id, password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val admin = searchUserId(id) ?: return CommonResult.notfound("User not found")

            if (!(admin.role == UserRole.ADMIN || admin.role == UserRole.SUPER_ADMIN)) {
                return CommonResult.unauthorized()
            }

            return CommonResult.data(resLogin(token = tokenProvider.createToken(authentication), userInfo = admin))
//			return LoginResponse(tokenProvider.createToken(request.email, role), userinfo)
        } catch (exception: AuthenticationException) {
            logger.error{ "Invalid username or password id ${id}"}
            return CommonResult.unprocessable()
        }
    }

    fun searchUserId(id: String): SiteUser? {
        return userRepository.findUsersById(id)
    }

    @Throws(CustomException::class)
    fun adminRegister(request: reqRegister) : CommonResult = with(request) {
        try {
            searchUserId(id)?.let {
                logger.error { "${id} user-id is already in use" }
                return CommonResult.exist(request,"user-id is already in use")
            }
            return CommonResult.data(userRepository.save(
                SiteUser(
                    idx = null,
                    id = id,
                    password = passwordEncoder.encode(password),
                    userName = userName,
                    userPhone = userPhone,
                    role = userRole!!)))

        } catch (e: CustomException) {
            logger.error { "admin register error $request ${e.message}" }
            return CommonResult.error("admin register error")
        }
    }

}