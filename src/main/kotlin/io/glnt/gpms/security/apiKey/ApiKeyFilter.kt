package io.glnt.gpms.security.apiKey

import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.security.CustomUserDetails
import io.glnt.gpms.security.apiKey.ApiKeyConfigurer.Companion.API_KEY_HEADER
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.SecurityConfigurerAdapter
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.core.Authentication
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.DefaultSecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.util.StringUtils
import org.springframework.web.filter.GenericFilterBean
import java.io.IOException
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest

class ApiKeyFilter(private val userDetailsService: CustomUserDetails): GenericFilterBean() {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(servletRequest: ServletRequest, servletResponse: ServletResponse, filterChain: FilterChain) {
        val httpServletRequest = servletRequest as HttpServletRequest
        val key = httpServletRequest.getHeader(API_KEY_HEADER)
        if (key != null && StringUtils.hasText(key)) {
            getAuthentication(key)?.let {
                SecurityContextHolder.getContext().authentication = it
            }
        }
        filterChain.doFilter(servletRequest, servletResponse)
    }

    private fun getAuthentication(key: String): Authentication {
        return userDetailsService.loadApiUser(key)?.let { it ->
            val authorities: Collection<GrantedAuthority?> = arrayListOf(SimpleGrantedAuthority("ROLE_API"))
            return UsernamePasswordAuthenticationToken(it, key, authorities)
        }

    }

}

class ApiKeyConfigurer(private val userDetailsService: CustomUserDetails): SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity>() {
    companion object {
        const val API_KEY_HEADER:String = "x-api-key"
    }

    override fun configure(builder: HttpSecurity) {
        val customFilter = ApiKeyFilter(userDetailsService)
        builder.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter::class.java)
    }
}
