package io.glnt.gpms.common.api

interface IErrorCode {
    fun getCode(): Long
    fun getMessage(): String
}