package io.glnt.gpms.security

import io.glnt.gpms.exception.ResourceNotFoundException
import io.glnt.gpms.model.repository.UserRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetails : UserDetailsService {
    companion object : KLogging()

    @Autowired
    private lateinit var userRepository: UserRepository

    @Throws(UsernameNotFoundException::class)
    override fun loadUserByUsername(username: String): UserDetails {

        logger.info{ "loadUserByUsername username: " + username }
        userRepository.findUsersByAdminId(username)?.let { profile ->
            logger.info{ "loadUserByUsername find email: " + profile.adminId }
            return UserPrincipal(profile)
//            return org.springframework.security.core.userdetails.User
//                .withUsername(profile.id.toString())
//                .password(profile.password)
//                .authorities(profile.role.name)
//                .accountExpired(false)
//                .accountLocked(false)
//                .credentialsExpired(false)
//                .disabled(false)
//                .build()
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

}