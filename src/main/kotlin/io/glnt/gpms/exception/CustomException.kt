package io.glnt.gpms.exception

import org.springframework.http.HttpStatus

class CustomException(
    override val message: String,
    val status: HttpStatus
) : RuntimeException(message)

class ResourceNotFoundException (
    override val message: String
) : RuntimeException(message)

class AlreadyExistsException(
    override val message: String,
    val status: HttpStatus
) : Exception()