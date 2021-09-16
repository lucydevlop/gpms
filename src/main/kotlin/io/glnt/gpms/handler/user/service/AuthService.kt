package io.glnt.gpms.handler.user.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.model.reqSearchItem
import io.glnt.gpms.handler.user.model.reqLogin
import io.glnt.gpms.handler.user.model.reqRegister
import io.glnt.gpms.handler.user.model.reqUserRegister
import io.glnt.gpms.handler.user.model.resLogin
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.dto.request.reqUserInfo
import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.entity.User
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.UserRepository
import io.glnt.gpms.security.jwt.JwtTokenProvider
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import okhttp3.internal.format
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import javax.persistence.criteria.Predicate
import javax.servlet.http.HttpServletRequest
import kotlin.math.log

@Service
class AuthService(
    private var parkSiteInfoService: ParkSiteInfoService
) {
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
        userRepository.findUsersById("glnt")?: run {
            userRepository.save(
                SiteUser(
                    idx = null,
                    id = "glnt",
                    password = passwordEncoder.encode("glnt123!@#"),
                    userName = "glnt",
                    userPhone = "0100000000",
                    role = UserRole.SUPER_ADMIN,
                    delYn = DelYn.N
                )
            )
        }
        userRepository.findUsersById("admin")?: run {
            userRepository.save(
                SiteUser(
                    idx = null,
                    id = "admin",
                    password = passwordEncoder.encode("glnt11!!"),
                    userName = "관리자",
                    userPhone = "0100000000",
                    role = UserRole.ADMIN,
                    delYn = DelYn.N
                )
            )
        }
        userRepository.findUsersById("rcs-user")?: run {
            userRepository.save(
                SiteUser(
                    idx = null,
                    id = "rcs-user",
                    password = passwordEncoder.encode("glnt123!@#"),
                    userName = "rcs",
                    userPhone = "0100000000",
                    role = UserRole.SUPER_ADMIN,
                    delYn = DelYn.N
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
            searchUserId(id)?.let {
                it.wrongCount = it.wrongCount?.plus(1)
                userRepository.save(it)
            }
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
                    role = userRole!!,
                    delYn = DelYn.N )))

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
            searchUserId(id)?.let {
                it.wrongCount = it.wrongCount?.plus(1)
                userRepository.save(it)
            }
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
                dong = dong, ho = ho, ceoName = userName, tel = userPhone, corpId = " ",
                delYn = DelYn.N
            ))
            corp.corpId = parkSiteInfoService.getParkSiteId()+"_"+ format("%05d", corp.sn!!)
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

    @Throws(CustomException::class)
    fun userRegisters(request: Array<reqUserRegister>) : CommonResult {
        try {
            val resultData: ArrayList<Corp> = ArrayList()
            request.filter { corpRepository.findByCorpNameAndCeoName(it.corpName,it.userName) == null }.let {
                it.forEach { req ->
                    var corp = corpRepository.save(Corp(
                        sn = null, corpName = req.corpName, form = req.form!!, resident = req.resident!!,
                        dong = req.dong, ho = req.ho, ceoName = req.userName, tel = req.userPhone, corpId = if (req.corpId == null || req.corpId.equals(" ") || req.corpId.equals(""))  "NoCorpId" else req.corpId,
                        delYn = DelYn.N
                    ))
                    if (!corp.corpId.equals("NoCorpId")) {
                        corpRepository.save(corp);
                    } else {
                        corp.corpId = parkSiteInfoService.getParkSiteId()+"_"+ format("%05d", corp.sn!!)
                        corp = corpRepository.save(corp)
                    }
                    userRepository.save(
                        SiteUser(
                            idx = null,
                            id = corp.corpId!!,
                            password = passwordEncoder.encode(req.password),
                            userName = req.userName,
                            userPhone = req.userPhone,
                            corpSn = corp.sn,
                            role = req.userRole!!
                        )
                    )
                    resultData.add(corp)
                }
            }
            return CommonResult.data(resultData)

            } catch (e: CustomException) {
                logger.error { "admin register error $request ${e.message}" }
            return CommonResult.error("admin register error")
            }
        }

    fun isAdmin(role: UserRole) : Boolean {
        return when(role) {
            UserRole.SUPER_ADMIN -> true
            UserRole.ADMIN -> true
            UserRole.OPERATION -> true
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
            return CommonResult.data(resLogin(userInfo = user, token = token, corpInfo = if (user.role==UserRole.STORE) corpRepository.findBySn(user.corpSn!!) else null))
        } catch (e: RuntimeException) {
            logger.error{"search user $idx error ${e.message}"}
            return CommonResult.unprocessable()
        }
    }

    fun searchUsers(request: reqSearchItem) : CommonResult {
        try {
            return CommonResult.data(userRepository.findAll(findAllUserSpecification(request)))
        }catch (e: RuntimeException) {
            logger.error{"search user ${request.searchRoles} error $e"}
            return CommonResult.error("search user ${request.searchRoles} error")
        }
    }

    fun  deleteUser(sn: Long): CommonResult {
        try{
            userRepository.findUserByIdx(sn)?.let {
                it.delYn = DelYn.Y
                userRepository.save(it)
            }
        }catch (e: CustomException) {
            logger.error{"deleteUser error $e"}
            return CommonResult.error("deleteUser user $sn error")
        }
        return CommonResult.data()
    }

    fun editUser(request: reqUserInfo): SiteUser?  {
        try {
            request.idx?.let { idx ->
                userRepository.findUserByIdx(idx)?.let { user ->
                    user.password = request.password?.let { passwordEncoder.encode(request.password) }?: run { user.password }
                    user.userName = request.userName?.let { it }?: run { user.userName }
                    user.role = request.role?.let { it }?: run { user.role }
                    user.userPhone = request.userPhone?.let{ it }?: run { user.userPhone }
                    return userRepository.saveAndFlush(user)
                }
            }
            return null
        }catch (e: CustomException) {
            logger.error{"deleteUser error $e"}
            return null
        }
    }

    private fun findAllUserSpecification(request: reqSearchItem) : Specification<SiteUser> {
        val spec = Specification<SiteUser> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.searchLabel == "USERID" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("id")), likeValue)
                )
            }

            if(request.searchLabel == "USERNAME" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("userName")), likeValue)
                )
            }

            if (request.searchRoles != null) {
                clues.add(
                    //criteriaBuilder.and(criteriaBuilder.`in`(root.get<Long>("ticketHistSn")), request.ticketsSn)
                    criteriaBuilder.and(root.get<String>("role").`in`(request.searchRoles!!.map { it }))
                )
//                clues.add(
//                    criteriaBuilder.equal(criteriaBuilder.lower(root.get<String>("role")), request.searchRole)
//                )
            }

            clues.add(criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N))


            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }

//    fun generateUserId(): String {
//        return parkinglotService.parkSite.siteid+"_"+ format("%04d", SiteUser.idx)
//    }
}