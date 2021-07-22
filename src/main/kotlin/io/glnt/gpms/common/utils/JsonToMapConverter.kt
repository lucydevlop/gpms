package io.glnt.gpms.common.utils

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import java.io.IOException
import javax.persistence.AttributeConverter

class JsonToMapConverter : AttributeConverter<String, HashMap<String, Any>> {

    companion object {
        private val LOGGER = LoggerFactory.getLogger(JsonToMapConverter::class.java)
    }

    override fun convertToDatabaseColumn(attribute: String?): HashMap<String, Any> {
        if(attribute == null) {
            return HashMap()
        }
        try {
            val objectMapper = ObjectMapper()
            @Suppress("UNCHECKED_CAST")
            return objectMapper.readValue(attribute, HashMap::class.java) as HashMap<String, Any>
        } catch (e: IOException) {
            LOGGER.error("Convert error while trying to convert string(JSON) to map data structure.")
        }
        return HashMap()
    }

    override fun convertToEntityAttribute(dbData: HashMap<String, Any>?): String? {
        return try {
            val objectMapper = ObjectMapper()
            objectMapper.writeValueAsString(dbData)
        } catch (e: JsonProcessingException) {
            LOGGER.error("Could not convert map to json string.")
            return null
        }
    }
}