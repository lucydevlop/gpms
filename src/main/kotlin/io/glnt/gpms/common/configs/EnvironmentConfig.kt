package io.glnt.gpms.common.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer

@Configuration
class EnvironmentConfig {

    companion object {
        @Bean
        fun propertySourcesPlaceholderConfigurer(): PropertySourcesPlaceholderConfigurer? {
            return PropertySourcesPlaceholderConfigurer()
        }
    }
}