package io.glnt.gpms.security.jwt

import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.SecurityConfig.TOKEN_EXPIRATION_TIME
import io.glnt.gpms.common.configs.SecurityConfig.TOKEN_HEADER
import io.glnt.gpms.common.configs.SecurityConfig.TOKEN_SECRET_KEY
import io.glnt.gpms.common.configs.SecurityConfig.TOKEN_TYPE
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.security.UserPrincipal
import io.jsonwebtoken.*
import mu.KLogging
import org.springframework.http.HttpStatus.UNAUTHORIZED
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import java.security.SignatureException
import java.util.*
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest

@Component
class JwtTokenProvider {
    companion object : KLogging()
    private var secretKey = TOKEN_SECRET_KEY
    private var expirationTime = TOKEN_EXPIRATION_TIME

    @PostConstruct
    protected fun init() {
        secretKey = Base64.getEncoder().encodeToString(secretKey.toByteArray())
    }

    //userPrincipal 반영 변경
    fun createToken(authentication: Authentication): String{
        val userPrincipal = authentication.principal as UserPrincipal

        val issuedAt = Date()
        val expiredIn = Date(issuedAt.time + expirationTime)

        return Jwts.builder()
            .setSubject(userPrincipal.idx.toString())
            .setIssuedAt(issuedAt)
            .setExpiration(expiredIn)
            .signWith(SignatureAlgorithm.HS512, secretKey)
            .compact()
    }

    fun userIdFromJwt(token: String): Long{
        return Jwts.parser()
            .setSigningKey(secretKey)
            .parseClaimsJws(token)
            .body
            .subject.toLong()
    }

    fun validateToken(token: String): Boolean{
        try {
            Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token)
            return true
        } catch (ex: SignatureException) {
            logger.error("Invalid JWT signature")
        } catch (ex: MalformedJwtException) {
            logger.error("Invalid JWT token")
        } catch (ex: ExpiredJwtException) {
            logger.error("Expired JWT token")
        } catch (ex: UnsupportedJwtException) {
            logger.error("Unsupported JWT token")
        } catch (ex: IllegalArgumentException) {
            logger.error("JWT claims string is empty.")
        }

        return false
    }

    fun resolveTokenOrNull(
        request: HttpServletRequest
    ) = request.getHeader(TOKEN_HEADER)?.removePrefix("$TOKEN_TYPE ")

    @Throws(CustomException::class)
    fun resolveTokenOrThrow(
        request: HttpServletRequest
    ) = resolveTokenOrNull(request)
        ?: throw CustomException("Invalid token", ResultCode.UNAUTHORIZED)



}