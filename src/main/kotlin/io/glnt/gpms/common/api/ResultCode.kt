package io.glnt.gpms.common.api

enum class ResultCode(private val code: Long, private val message: String) : IErrorCode {
    SUCCESS(200, "SUCCESS"),
    CREATED(201, "CREATED"),
    FAILED(500, "FAILED"),
    VALIDATE_FAILED(404, "Data not found"),
    UNAUTHORIZED(401, "UNAUTHORIZED"),
    CONFLICT(409, "CONFLICT"),
    UNPROCESSABLE_ENTITY(422, "UNPROCESSABLE_ENTITY"),
    FORBIDDEN(403, "403");

    override fun getCode(): Long {
        return code
    }

    override fun getMessage(): String {
        return message
    }
}