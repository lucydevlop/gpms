package io.glnt.gpms.security

import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.exception.ResourceNotFoundException
import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.repository.UserRepository
import mu.KLogging
import org.apache.commons.codec.binary.Hex
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.*

@Service
class CustomUserDetails : UserDetailsService {
    companion object : KLogging()

    @Autowired
    private lateinit var userRepository: UserRepository

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {

        logger.info{ "loadUserByUsername username: " + username }
        userRepository.findUsersById(username)?.let { profile ->
            logger.info{ "loadUserByUsername find id: " + profile.id }
            return UserPrincipal(profile)
        }

        logger.info{ "loadUserByUsername find not email: " + username }
        throw UsernameNotFoundException("User '$username' not found")
    }

    //    @Transactional
    fun loadUserById(id: Long): UserDetails {
        val user = userRepository.findById(id).orElseThrow {
            ResourceNotFoundException("User '$id' not found")
        }
        return UserPrincipal(user)
    }

    fun loadApiUser(key: String) : UserDetails {
        val decodeStr = Base64Util.decodeAsString(key)
        val str = decodeStr.split(":").toTypedArray()
        if (str.size == 0) throw UsernameNotFoundException("User not found")
        if ((str[0] == "api-user" || str[0] == "rcs-user") && (str[1] == ("apiuser11!!"))) {
            return UserPrincipal(
                SiteUser(
                    idx = 0,
                    id = str[0],
                    password = str[1],
                    userName = str[0],
                    role = UserRole.API,
                    userPhone = "00000000"
                )
            )
        }
        logger.info{ "loadApiUser find not : " + key }
        throw UsernameNotFoundException("User not found")
    }

}