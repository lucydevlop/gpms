package io.glnt.gpms.exception

import io.glnt.gpms.common.api.ResultCode
import org.springframework.http.HttpStatus

class CustomException(
    val msg: String,
    val code: ResultCode
) : RuntimeException(msg)

class ResourceNotFoundException (
    val msg: String
) : RuntimeException(msg)

class AlreadyExistsException(
    val msg: String,
    val status: HttpStatus
) : Exception()

class RejectException(
    val msg: String,
    val code: ResultCode
) : RuntimeException(msg)