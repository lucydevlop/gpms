package io.glnt.gpms.handler.dashboard.user.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.user.model.*
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.inout.service.checkItemsAre
import io.glnt.gpms.handler.relay.model.paystationvehicleListSearch
import io.glnt.gpms.model.entity.ParkIn
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

@Service
class DashboardUserService {
    companion object : KLogging()

    @Autowired
    private lateinit var inoutService: InoutService


    @Throws(CustomException::class)
    fun parkingDiscountSearchVehicle(request: reqVehicleSearch) : CommonResult {
        logger.info { "parkingDiscountSearchVehicle $request" }
        try {
            val parkins = inoutService.searchParkInByVehicleNo(request.vehicleNo, "")
            val data = ArrayList<resVehicleSearch>()
            when(parkins.code) {
                ResultCode.SUCCESS.getCode() -> {
                    val lists = parkins.data as? List<*>?
                    lists!!.checkItemsAre<ParkIn>()?.filter { it.outSn == 0L }?.let { list ->
                        list.forEach {
                            logger.debug { "image path ${it.image!!.substring(it.image!!.indexOf("/park"))}" }
                            data.add(
                                resVehicleSearch(
                                    sn = it.sn!!,
                                    vehicleNo = it.vehicleNo!!,
                                    inDate = DateUtil.formatDateTime(it.inDate!!, "yyyy-MM-dd HH:mm:ss"),
                                    inImgBase64Str = it.image!!.substring(it.image!!.indexOf("/park")) )
                            )
                        }

                    }
                }
            }
            return CommonResult.data(data)
        }catch (e: CustomException){
            logger.error { "parkingDiscountSearchVehicle failed ${e.message}" }
            return CommonResult.error("parkingDiscountSearchVehicle failed ${e.message}")
        }
    }
}