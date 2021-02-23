package io.glnt.gpms.handler.dashboard.admin.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.inout.service.InoutService
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DashboardAdminService {
    companion object : KLogging()

    @Autowired
    lateinit var inoutService: InoutService


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

    @Throws(CustomException::class)
    fun getParkInLists(request: reqSearchParkin): CommonResult {
        try {
            return CommonResult.data(inoutService.getAllParkLists(request).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }
}