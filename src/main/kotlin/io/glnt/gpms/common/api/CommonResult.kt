package io.glnt.gpms.common.api

import io.glnt.gpms.common.api.IErrorCode
import io.glnt.gpms.common.api.ResultCode

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
                it.code = 409
            }
        }

        fun error(code: Int = 500, error: Exception): CommonResult {
            return CommonResult().also {
                it.msg = error.message
                it.code = code
            }
        }
    }

}