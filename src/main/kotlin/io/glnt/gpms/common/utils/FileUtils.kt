package io.glnt.gpms.common.utils

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat

object FileUtils {

    fun writeDatesToJsonFile(content: Any, pathToSave: String) {
        val mapper = ObjectMapper()
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        File(pathToSave).writeText(mapper.writeValueAsString(content))
        //createFile(pathToSave).writeText(mapper.writeValueAsString(content))
    }

    fun createFile(path: String) : File {
        val pathToFile = Paths.get(path)
        Files.createDirectories(pathToFile.parent)
        Files.deleteIfExists(pathToFile)
        Files.createFile(pathToFile)
        return pathToFile.toFile()
    }
}