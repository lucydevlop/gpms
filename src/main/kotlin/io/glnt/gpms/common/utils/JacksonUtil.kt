package io.glnt.gpms.common.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.IOException

object JacksonUtil {
    val OBJECT_MAPPER = ObjectMapper()

    fun <T> fromString(string: String, clazz: Class<T>?): T {
        return try {
            OBJECT_MAPPER.readValue(string, clazz)
        } catch (e: IOException) {
            throw IllegalArgumentException("The given string value: $string cannot be transformed to Json object")
        }
    }

    fun toString(value: Any): String {
        return try {
            OBJECT_MAPPER.writeValueAsString(value)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("The given Json object value: $value cannot be transformed to a String")
        }
    }

    fun toJsonNode(value: String?): JsonNode? {
        return try {
            OBJECT_MAPPER.readTree(value)
        } catch (e: IOException) {
            throw IllegalArgumentException(e)
        }
    }

//    fun <T> clone(value: T): T {
//        return fromString(toString(value), value.javaClass as Class<T>)
//    }
}