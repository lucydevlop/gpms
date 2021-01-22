package io.glnt.gpms.security

import io.glnt.gpms.common.configs.ApiConfig.API_VERSION
import io.glnt.gpms.common.configs.SecurityConfig.PASSWORD_STRENGTH
import io.glnt.gpms.security.jwt.JwtAuthenticationEntryPoint
import io.glnt.gpms.security.jwt.JwtAuthenticationFilter
import io.glnt.gpms.security.jwt.JwtTokenProvider
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.BeanIds
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class WebSecurityConfig : WebSecurityConfigurerAdapter() {
    @Autowired
    private lateinit var tokenProvider: JwtTokenProvider

    @Autowired
    private lateinit var userDetails: CustomUserDetails

    @Bean
    fun jwtAuthenticationFilter(): JwtAuthenticationFilter = JwtAuthenticationFilter(tokenProvider, userDetails)

    @Bean(BeanIds.AUTHENTICATION_MANAGER)
    override fun authenticationManagerBean(): AuthenticationManager = super.authenticationManagerBean()

//    override fun configure(builder: AuthenticationManagerBuilder) {
//        builder.inMemoryAuthentication()
//            .withUser("user")//.password(password)
//            .roles("USER")
//            .and()
//            .passwordEncoder(passwordEncoder())
//    }

    @Throws(Exception::class)
    override fun configure(http: HttpSecurity): Unit = with(http) {
        csrf().disable()

        sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//        exceptionHandling().authenticationEntryPoint(JwtAuthenticationEntryPoint())

        authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            .antMatchers("/$API_VERSION/auth/**").permitAll()
            .antMatchers("/$API_VERSION/parkinglot/**").permitAll()
            .antMatchers("/$API_VERSION/corp/**").permitAll()
            .antMatchers("/$API_VERSION/facility/**").permitAll()
            .antMatchers("/$API_VERSION/manage/**").permitAll()
            .antMatchers("/$API_VERSION/inout/**").permitAll()
            .antMatchers("/$API_VERSION/relay/**").permitAll()
            .antMatchers("/api/**").permitAll()
//            .antMatchers("/$API_VERSION/$CORE_PATH/**").permitAll()
//			.antMatchers("/$API_VERSION/$SHOP_PATH/**").permitAll()
            .anyRequest().authenticated()

//		apply(JwtConfigurerAdapter(tokenProvider))
        addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter::class.java)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder(PASSWORD_STRENGTH)

}