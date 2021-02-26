package io.glnt.gpms.handler.user.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.user.model.reqLogin
import io.glnt.gpms.handler.user.model.reqRegister
import io.glnt.gpms.handler.user.model.reqUserRegister
import io.glnt.gpms.handler.user.model.resLogin
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.UserRepository
import io.glnt.gpms.security.jwt.JwtTokenProvider
import mu.KLogging
import okhttp3.internal.format
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

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

    @Autowired
    private lateinit var corpRepository: CorpRepository

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @PostConstruct
    fun addFirstAdminUser() {
        if (userRepository.findUsersById("glnt") == null) {
            userRepository.save(
                SiteUser(
                    idx = null,
                    id = "glnt",
                    password = passwordEncoder.encode("glnt123!@#"),
                    userName = "glnt",
                    userPhone = "0100000000",
                    role = UserRole.SUPER_ADMIN
                )
            )
        }
    }

    fun adminLogin(request: reqLogin) : CommonResult = with(request) {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(id, password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val admin = searchUserId(id) ?: return CommonResult.notfound("User not found")

            if (!isAdmin(admin.role!!)) {
                return CommonResult.unauthorized()
            }

            // login_date update
            admin.loginDate = LocalDateTime.now()
            userRepository.save(admin)

            return CommonResult.data(resLogin(token = tokenProvider.createToken(authentication), userInfo = admin))
        } catch (exception: AuthenticationException) {
            logger.error{ "Invalid username or password id ${id}"}
            return CommonResult.unprocessable()
        }
    }

    fun searchUserId(id: String): SiteUser? {
        return userRepository.findUsersById(id)
    }

    fun searchUserByIdx(idx: Long): SiteUser? {
        return userRepository.findUserByIdx(idx)
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

    fun userLogin(request: reqLogin) : CommonResult = with(request) {
        try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(id, password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val user = searchUserId(id) ?: return CommonResult.notfound("User not found")

            if (isAdmin(user.role!!) || (user.corpSn == null || user.corpSn!! < 1)) {
                return CommonResult.unauthorized()
            }

            // login_date update
            user.loginDate = LocalDateTime.now()
            userRepository.save(user)

            return CommonResult.data(resLogin(token = tokenProvider.createToken(authentication), userInfo = user, corpInfo =  corpRepository.findBySn(user.corpSn!!)))
        } catch (exception: AuthenticationException) {
            logger.error{ "Invalid username or password id ${id}"}
            return CommonResult.unprocessable()
        }
    }

    @Throws(CustomException::class)
    fun userRegister(request: reqUserRegister) : CommonResult = with(request) {
        try {
            corpRepository.findByCorpNameAndCeoName(corpName, userName)?.let {
                logger.error { "${corpName} store is already in use" }
                return CommonResult.exist(request,"store is already in use")
            }
            var corp = corpRepository.save(Corp( 
                sn = null, corpName = corpName, form = form!!, resident = resident!!,
                dong = dong, ho = ho, ceoName = userName, tel = userPhone, corpId = null
            ))
            corp.corpId = parkinglotService.parkSite.siteid+"_"+ format("%05d", corp.sn!!)
            corp = corpRepository.save(corp)
            return CommonResult.data(userRepository.save(
                SiteUser(
                    idx = null,
                    id = corp.corpId!!,
                    password = passwordEncoder.encode(password),
                    userName = userName,
                    userPhone = userPhone,
                    corpSn = corp.sn,
                    role = userRole!!)))
        } catch (e: CustomException) {
            logger.error { "admin register error $request ${e.message}" }
            return CommonResult.error("admin register error")
        }
    }

    fun isAdmin(role: UserRole) : Boolean {
        return when(role) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> true
            else -> false
        }
    }

    fun getUser(userId: String): CommonResult {
        try {
            val user = searchUserId(userId) ?: return CommonResult.notfound("User not found")
            return CommonResult.data(resLogin(userInfo = user, corpInfo = if (user.role==UserRole.STORE) corpRepository.findByCorpId(user.id) else null))
        } catch (e: RuntimeException) {
            logger.error{"search user $userId error ${e.message}"}
            return CommonResult.unprocessable()
        }
    }

    fun getToken(servlet: HttpServletRequest) : CommonResult {
        var idx = 0L
        try {
            val token = jwtTokenProvider.resolveTokenOrNull(servlet)
            idx = jwtTokenProvider.userIdFromJwt(token!!)
            val user = searchUserByIdx(idx) ?: return CommonResult.notfound("User not found")
            return CommonResult.data(resLogin(userInfo = user, token = token))
        } catch (e: RuntimeException) {
            logger.error{"search user $idx error ${e.message}"}
            return CommonResult.unprocessable()
        }
    }

//    fun generateUserId(): String {
//        return parkinglotService.parkSite.siteid+"_"+ format("%04d", SiteUser.idx)
//    }
}