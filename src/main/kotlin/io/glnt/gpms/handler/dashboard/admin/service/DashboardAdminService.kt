package io.glnt.gpms.handler.dashboard.admin.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class DashboardAdminService {
    companion object : KLogging()


    @Throws(CustomException::class)
    fun getMainGates(): CommonResult {
        try {

            // gate 당 입출차 내역 조회
            // 각 장비 상태 조회
            return CommonResult.data("")
        }catch (e: CustomException){
            logger.error { "Admin getMainGates failed ${e.message}" }
            return CommonResult.error("Admin getMainGates failed ${e.message}")
        }
    }
}