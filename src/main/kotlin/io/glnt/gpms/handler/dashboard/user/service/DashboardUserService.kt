package io.glnt.gpms.handler.dashboard.user.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.user.model.*
import io.glnt.gpms.handler.discount.model.reqDiscountableTicket
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.inout.service.checkItemsAre
import io.glnt.gpms.handler.relay.model.paystationvehicleListSearch
import io.glnt.gpms.model.entity.CorpTicket
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

    @Autowired
    private lateinit var discountService: DiscountService
    
    @Throws(CustomException::class)
    fun parkingDiscountSearchVehicle(request: reqVehicleSearch) : CommonResult {
        try {
            val parkins = inoutService.searchParkInByVehicleNo(request.vehicleNo, "")
            val data = ArrayList<resVehicleSearch>()
            when(parkins.code) {
                ResultCode.SUCCESS.getCode() -> {
                    val lists = parkins.data as? List<*>?
                    lists!!.checkItemsAre<ParkIn>()?.filter { it.outSn == 0L }?.let { list ->
                        list.forEach {
//                            logger.debug { "image path ${it.image!!.substring(it.image!!.indexOf("/park"))}" }
                            data.add(
                                resVehicleSearch(
                                    sn = it.sn!!,
                                    vehicleNo = it.vehicleNo!!,
                                    inDate = DateUtil.formatDateTime(it.inDate!!, "yyyy-MM-dd HH:mm:ss"),
                                    imImagePath = it.image!!.substring(it.image!!.indexOf("/park")) )
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

    @Throws(CustomException::class)
    fun parkingDiscountSearchTicket(request: reqParkingDiscounTicketSearch) : CommonResult {
        try {
            val parkins = inoutService.searchParkInByVehicleNo(request.vehicleNo, "")
            val data = ArrayList<resVehicleSearch>()
            when(parkins.code) {
                ResultCode.SUCCESS.getCode() -> {
                    val lists = parkins.data as? List<*>?
                    lists!!.checkItemsAre<ParkIn>()?.filter { it.outSn == 0L }?.let { list ->
                        list.forEach {
//                            logger.debug { "image path ${it.image!!.substring(it.image!!.indexOf("/park"))}" }
                            data.add(
                                resVehicleSearch(
                                    sn = it.sn!!,
                                    vehicleNo = it.vehicleNo!!,
                                    inDate = DateUtil.formatDateTime(it.inDate!!, "yyyy-MM-dd HH:mm:ss"),
                                    imImagePath = it.image!!.substring(it.image!!.indexOf("/park")) )
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

    @Throws(CustomException::class)
    fun parkingDiscountAbleTickets(request: reqParkingDiscountAbleTicketsSearch) : CommonResult {
        try {
            val discountTickets = discountService.getDiscountableTickets(reqDiscountableTicket(corpId = request.corpId, date = request.inDate, inSn = request.inSn))
            when(discountTickets.code) {
                ResultCode.SUCCESS.getCode() -> {
                    discountTickets.data?.let {
                        val lists = discountTickets.data as List<CorpTicket>
                        return CommonResult.data(lists)
                    }
                    return CommonResult.data()
                }
                ResultCode.VALIDATE_FAILED.getCode() -> {
                    return CommonResult.notfound("ticket not found")
                }
                else -> return CommonResult.error("ticket not found")
            }

        }catch (e: CustomException){
            logger.error { "parkingDiscountAbleTickets failed ${e.message}" }
            return CommonResult.error("parkingDiscountAbleTickets failed ${e.message}")
        }
    }
}