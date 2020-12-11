package io.glnt.gpms.common.utils

import java.util.*

object Base64Util {

    fun decodeAsBytes(bytes: String): ByteArray? {
        return Base64.getDecoder().decode(bytes)
    }

    fun decodeAsString(bytes: String): String {
        return String(Base64.getDecoder().decode(bytes))
    }

    fun decode(bytes: ByteArray): ByteArray {
        return Base64.getDecoder().decode(bytes)
    }

    fun encodeAsString(text: String): String {
        return encodeAsString(text.toByteArray())
    }

    fun encodeAsString(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    fun encode(bytes: ByteArray): ByteArray {
        return Base64.getEncoder().encode(bytes)
    }
}