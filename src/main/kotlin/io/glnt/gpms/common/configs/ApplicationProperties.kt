package io.glnt.gpms.common.configs

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
open class ApplicationProperties {

}

//@Configuration
//@EnableConfigurationProperties(ApplicationProperties::class)
//class ApplicationConfiguration {
//
//}
//
//@ConstructorBinding
//@ConfigurationProperties(prefix = "application")
//data class ApplicationProperties(