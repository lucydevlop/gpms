package io.glnt.gpms.common.configs

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

@Configuration
class JacksonConfig {
    @Bean
    @Primary
    open fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
        val objectMapper = builder.createXmlMapper(false).build<ObjectMapper>()

        objectMapper.registerModule(KotlinModule())
        objectMapper.registerModule(ParameterNamesModule())
        objectMapper.registerModule(Jdk8Module())
        objectMapper.registerModule(JavaTimeModule())

        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)

        return objectMapper
    }
//
//    @Bean
//    fun objectMapperBuilder(): Jackson2ObjectMapperBuilder {
//        val mapper = ObjectMapper()
//        mapper.registerModule(KotlinModule())
//        val builder = Jackson2ObjectMapperBuilder()
//        builder.serializationInclusion(JsonInclude.Include.NON_NULL)
//        builder.serializerByType(LocalDate::class.java, LocalDateSerializer())
//        builder.serializerByType(LocalDateTime::class.java, LocalDateTimeSerializer())
//        return builder
//    }
//
//    /**
//     * Customer type converter
//     */
//    class LocalDateTimeSerializer : JsonSerializer<LocalDateTime>() {
//
//        override fun serialize(p0: LocalDateTime?, p1: JsonGenerator?, p2: SerializerProvider?) {
//            p1!!.writeString(p0!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
////            p1!!.writeNumber(p0!!.toInstant(ZoneOffset.UTC).toEpochMilli())
//        }
//    }
//
//    /**
//     *
//     */
//    class LocalDateSerializer : JsonSerializer<LocalDate>() {
//
//        override fun serialize(p0: LocalDate?, p1: JsonGenerator?, p2: SerializerProvider?) {
//            p1!!.writeString(p0!!.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
//        }
//    }
}