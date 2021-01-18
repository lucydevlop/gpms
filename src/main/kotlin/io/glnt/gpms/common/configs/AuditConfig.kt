package io.glnt.gpms.common.configs

import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.entity.User
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

//@Configuration
//@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
//class AuditConfig {
//    @Bean
//    fun auditorProvider() : AuditorAware<String> {
//        return AuditorAware { Optional.of("") }
//    }
//}

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
class AuditConfig {
//    @Bean
//    fun auditorAware() = AuditorAware<String> {
//        SecurityContextHolder.getContext().authentication.name
//    }
    @Bean
    fun auditorAware(): SecurityAuditorAware {
        return SecurityAuditorAware()
    }
}

class SecurityAuditorAware: AuditorAware<String> {
    override fun getCurrentAuditor(): Optional<String> {
        val authentication = SecurityContextHolder.getContext().authentication

        if (authentication.isAuthenticated) {
//            return Optional.of(authentication.principal as SiteUser)
            val user = authentication.principal as UserDetails
            return Optional.of(user.username)
        }

        return Optional.of("")
    }
}