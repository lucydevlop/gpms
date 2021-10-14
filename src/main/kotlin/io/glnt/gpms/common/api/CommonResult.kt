package io.glnt.gpms.common.api

import io.glnt.gpms.common.api.IErrorCode
import io.glnt.gpms.common.api.ResultCode
import org.springframework.core.io.FileSystemResource
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import java.io.File
import java.io.InputStream

class CommonResult {
    var code : Any? = 200
    var msg: String? = null
    var data: Any? = null

    companion object {
        fun data(message: String = "success"): CommonResult {
            return CommonResult().also {
                it.code = ResultCode.SUCCESS.getCode()
                it.msg = message
            }
        }

        fun data(data: Any?, message: String = "success"): CommonResult {
            return CommonResult().also {
                it.code = ResultCode.SUCCESS.getCode()
                it.data = data
                it.msg = message
            }
        }

        fun created(message: String = "created"): CommonResult {
            return CommonResult().also {
                it.code = ResultCode.CREATED.getCode()
                it.msg = message
            }
        }

        fun error(error: String): CommonResult {
            return CommonResult().also {
                it.msg = error
                it.code = ResultCode.FAILED.getCode()
            }
        }

        fun error(code: Any, error: String): CommonResult {
            return CommonResult().also {
                it.msg = error
                it.code = code
            }
        }

        fun notfound(error: String) : CommonResult {
            return CommonResult().also {
                it.msg = error+ " data not found"
                it.code = ResultCode.VALIDATE_FAILED.getCode()
            }
        }

        fun exist(data: Any?, error: String) : CommonResult {
            return CommonResult().also {
                it.msg = error
                it.data = data
                it.code = ResultCode.CONFLICT.getCode()
            }
        }

        fun error(code: Int = 500, error: Exception): CommonResult {
            return CommonResult().also {
                it.msg = error.message
                it.code = code
            }
        }

        fun unauthorized() : CommonResult {
            return CommonResult().also {
                it.msg = "Invalid User login"
                it.code = ResultCode.UNAUTHORIZED.getCode()
            }
        }

        fun unprocessable() : CommonResult {
            return CommonResult().also {
                it.msg = "Invalid username or password"
                it.code = ResultCode.UNPROCESSABLE_ENTITY.getCode()
            }
        }

        fun returnResult(result: CommonResult): ResponseEntity<CommonResult> {
            return when(result.code) {
                ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
                ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.OK)
                ResultCode.VALIDATE_FAILED.getCode() -> ResponseEntity(result, HttpStatus.NOT_FOUND)
                ResultCode.CONFLICT.getCode() -> ResponseEntity(result, HttpStatus.CONFLICT)
                else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
            }
        }
        fun returnFile(result: CommonResult, name: String): ResponseEntity<FileSystemResource?> {
            val headers = HttpHeaders()
            headers.add("Cache-Control", "no-cache, no-store, must-revalidate")
            headers.add("content-disposition", String.format("attachment; filename=$name.csv"))
            headers.add("filename", String.format("attachment; filename=$name.csv"))
            headers.add("Pragma", "no-cache")
            headers.add("Expires", "0")

            when(result.code) {
                ResultCode.SUCCESS.getCode() -> {
                    val file: File = result.data as File
                    val filePath = file.absolutePath
                    val excelFile = FileSystemResource(filePath)
                    return ResponseEntity
                        .ok()
                        .header("Content-type", "application/octet-stream")
                        .header("Content-disposition", "attachment; filename=\"$name.csv\"")
                        .contentLength(excelFile.contentLength())
//                        .contentType(MediaType.parseMediaType("application/octet-stream"))
                        .body(excelFile)
                }
                else -> return ResponseEntity
                    .badRequest()
                    .headers(headers)
                    .contentLength(0)
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .body(null)
            }
        }
    }

}