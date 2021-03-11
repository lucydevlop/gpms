package io.glnt.gpms.handler.dashboard.user.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.user.model.*
import io.glnt.gpms.handler.discount.model.reqAddInoutDiscount
import io.glnt.gpms.handler.discount.model.reqDiscountableTicket
import io.glnt.gpms.handler.discount.model.reqSearchDiscount
import io.glnt.gpms.handler.discount.model.reqSearchInoutDiscount
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.inout.service.checkItemsAre
import io.glnt.gpms.model.entity.CorpTicket
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.TicketType
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.Integer.min
import java.util.*
import java.util.Comparator.comparing
import java.util.stream.Collectors
import java.util.stream.Collectors.maxBy
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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
                                    imImagePath = it.image!!.substring(it.image!!.indexOf("/park"))
                                )
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
                                    imImagePath = it.image!!.substring(it.image!!.indexOf("/park"))
                                )
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
            val discountTickets = if (request.inSn == null){
                discountService.getByCorp(reqSearchDiscount(corpId = request.corpId))
            } else {
                discountService.getDiscountableTickets(
                    reqDiscountableTicket(
                        corpId = request.corpId,
                        date = request.inDate,
                        inSn = request.inSn
                    )
                )
            }
            when(discountTickets.code) {
                ResultCode.SUCCESS.getCode() -> {
                    discountTickets.data?.let {
                        val lists = discountTickets.data as List<CorpTicket>
                        val groupedData: Map<DiscountClass, List<CorpTicket>> =
                            lists.stream().collect(Collectors.groupingBy { it.discountClass!! })
                        val result = ArrayList<HashMap<String, Any?>>()
                        groupedData.forEach { data ->
                            result.add(hashMapOf(
                                "discountName" to data.key.discountNm,
                                "dayRange" to data.key.dayRange,
                                "timeRange" to data.key.timeRange,
                                "timeTarget" to data.key.timeTarget,
                                "onceMax" to data.key.disMaxNo,
                                "dayMax" to data.key.disMaxDay,
                                "monthMax" to data.key.disMaxMonth,
                                "ableCnt" to data.value.sumBy { it.ableCnt!! }
                            ))
                        }
                        return CommonResult.data(result)
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

    @Throws(CustomException::class)
    fun parkingDiscountAddTicket(request: reqParkingDiscountAddTicket): CommonResult {
        try{
            // 적합여부 확인
            // Once 가능 횟수 > Day > Month
            var useCnt = request.cnt
            discountService.getInoutDiscount(reqSearchInoutDiscount(ticketSn = request.discountClassSn, inSn = request.inSn))?.let {
                if (it < request.cnt) return CommonResult.error("Exceeded the number of possible discounts")
                do {
                    discountService.getDiscountableTicketsByCorp(request.corpSn, request.discountClassSn)?.let { corpTicket ->
                        useCnt = min(corpTicket.ableCnt!!, request.cnt)
                        discountService.addInoutDiscount(reqAddInoutDiscount(inSn = request.inSn, discountType = TicketType.CORPTICKET, ticketSn = corpTicket.sn!!, quantity = useCnt))
                        corpTicket.ableCnt = corpTicket.ableCnt?.minus(useCnt)
                        discountService.updateCorpTicket(corpTicket)
                    }
                    request.cnt = request.cnt - useCnt
                }while(request.cnt > 0)
                return CommonResult.data()
            }?.run {
                return CommonResult.Companion.error("No discount available")
            }
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "parkingDiscountAbleTickets failed $e" }
            return CommonResult.error("parkingDiscountAddTicket failed ${e.message}")
        }
    }
}