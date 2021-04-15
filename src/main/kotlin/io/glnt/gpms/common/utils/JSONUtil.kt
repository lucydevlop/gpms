package io.glnt.gpms.common.utils

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.boot.configurationprocessor.json.JSONArray
import org.springframework.boot.configurationprocessor.json.JSONException
import org.springframework.boot.configurationprocessor.json.JSONObject
import java.io.InputStream
import java.time.LocalDateTime
import java.util.*

object JSONUtil {
    fun getJsObject(obj: Any?): Any {
        return if (obj is Map<*, *>) {
            getJsMap(obj)
        } else if (obj is Collection<*>) {
            getJsCollection(obj)
        } else {
            getJsValue(obj)
        }
    }



    fun getJsMap(map: Map<*, *>): Any {
        val buf = StringBuffer()
        buf.append("{")
        val iter = map.entries.iterator()
        if (iter.hasNext()) {
            val ety = iter.next() as java.util.Map.Entry<*, *>
            buf.append(getJsValue(ety.key))
            buf.append(":")
            buf.append(getJsObject(ety.value))
        }
        while (iter.hasNext()) {
            val ety = iter.next() as java.util.Map.Entry<*, *>
            buf.append(",")
            buf.append(getJsValue(ety.key))
            buf.append(":")
            buf.append(getJsObject(ety.value))
        }
        buf.append("}")
        return buf
    }

    fun getJsCollection(list: Collection<*>): Any {
        val buf = StringBuffer()
        buf.append("[")
        val iter = list.iterator()
        if (iter.hasNext()) {
            buf.append(getJsObject(iter.next()))
        }
        while (iter.hasNext()) {
            buf.append(",")
            buf.append(getJsObject(iter.next()))
        }
        buf.append("]")
        return buf
    }

    fun getJsString(obj: Any?): String {
        var obj = obj
        if (obj == null) {
            obj = ""
        }
        return obj.toString().replace("\\\\".toRegex(), "\\\\\\\\")
            .replace("'".toRegex(), "\\\\\'")
    }

    fun getJsValue(objValue: Any?): Any {
        val buf = StringBuffer()
//        buf.append("'")
        buf.append("\"")
        buf.append(getJsString(objValue))
//        buf.append("'")
        buf.append("\"")
        return buf
    }

    fun getJSONArray(jsonObject: JSONObject, name: String): JSONArray? {
        var array: JSONArray? = null
        val obj = getJSONObjectValue(jsonObject, name)
        try {
            array = JSONArray(obj.toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return array
    }

    fun getJSONObject(map: Map<*, *>): JSONObject {
        return JSONObject(map)
    }

    fun getJSONArray(collection: Collection<*>): JSONArray {
        return JSONArray(collection)
    }

    fun getJSONObject(jsonStr: String): JSONObject {
        var `object` = JSONObject()
        try {
            `object` = JSONObject(jsonStr)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return `object`
    }

    fun getJSONObjectValue(jsonObject: JSONObject, name: String): Any {
        var `object` = Any()
        try {
            `object` = jsonObject.get(name)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return `object`
    }

    fun getJSONObjectStringValue(jsonObject: JSONObject, name: String): String {
        var `object` = ""
        try {
            if (jsonObject.has(name)) `object` = jsonObject.getString(name)
        } catch (e: JSONException) {
        }

        return `object`
    }

    fun getJSONObjectLongValue(jsonObject: JSONObject, name: String): Long {
        var `object` = 0L
        try {
            if (jsonObject.has(name)) `object` = jsonObject.getString(name).toLongOrNull()!!
        } catch (e: JSONException) {
        }

        return `object`
    }

    fun getJSONObjectDoubleValue(jsonObject: JSONObject, name: String): Double {
        var `object` = 0.0
        try {
            if (jsonObject.has(name)) `object` = jsonObject.getString(name).toDoubleOrNull()!!
        } catch (e: JSONException) {
        }

        return `object`
    }

    fun getJSONObjectDateTimeValue(jsonObject: JSONObject, name: String): LocalDateTime {
        var `object`: LocalDateTime? = null
        try {
            if (jsonObject.has(name)) `object` = DateUtil.stringToLocalDateTime(jsonObject.getString(name))
        } catch (e: JSONException) {
        }

        return `object`!!
    }

    fun getJSONValue(jsonObject: JSONObject?, name: String): String? {
        var `object`: String? = null
        try {
            if (jsonObject != null && jsonObject.has(name)) {
                `object` = jsonObject.get(name).toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return `object`
    }

    fun getJSONObject(jsonObject: JSONObject, name: String): JSONObject {
        var `object` = JSONObject()
        try {
            `object` = JSONObject(jsonObject.get(name).toString())
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return `object`
    }

    fun getJSONObjectByIndex(array: JSONArray, index: Int): JSONObject {
        var jsonObject = JSONObject()
        try {
            jsonObject = array.getJSONObject(index)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return jsonObject
    }

    fun getJSONArray(sString: String?): JSONArray? {
        var mArray: JSONArray? = null
        try {
            if (sString != null && sString.trim { it <= ' ' }.length > 0)
                mArray = JSONArray(sString)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return mArray
    }

    fun generateRandomBasedUUID(): String {
        return UUID.randomUUID().toString()
    }

    fun <T : Any> readValue(any: String, valueType: Class<T>): T {
        val data = getJSONObject(any)
        val factory = JsonFactory()
        factory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES)
        return jacksonObjectMapper().readValue(data.toString(), valueType)
    }
}
