package io.glnt.gpms.handler.calc.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.CalculationData
import io.glnt.gpms.handler.calc.model.*
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.service.HolidayService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.model.dto.request.ReqAddParkingDiscount
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.enums.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime
import kotlin.math.ceil


@Service
class FeeCalculation {
    companion object : KLogging()

    @Autowired
    private lateinit var holidayService: HolidayService

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var calcData: CalculationData

    @Autowired
    private lateinit var discountService: DiscountService

    fun init() {
        calcData.init()
    }

    //return -1은 오류
    /**
     * @param inTime 입차시간
     * @param outTime 출차시간
     * @param vehicleType 차종
     * @param vehicleNo 차번호
     * @param type  0 : 할인없음, 1 : 전방시간할인, 2 : 후방시간 할인
     * @param discountMin 할인이 있을때 해당시간 설정, 없으면 0으로 호출
     * @return 요금정보관련클래스
     */
    fun getBasicPayment(
        inTime: LocalDateTime, outTime: LocalDateTime?,
        vehicleType: VehicleType, vehicleNo: String?,
        type: Int, discountMin: Int, inSn: Long?
    )
    : BasicPrice? {
        if (outTime == null) return null

        logger.info { "-------------------getBasicPayment-------------------" }
        logger.info { "입차시간 : $inTime / 출차시간 : $outTime" }
        logger.info { "차종 : $vehicleType / 차량번호 : $vehicleNo" }
        logger.info { "type(0:할인없음, 1:전방시간할인, 2:후방시간할인) : $type / 할인있을 때의 해당 시간 설정 : $discountMin" }
        logger.info { "-------------------getBasicPayment-------------------" }
        val retPrice = BasicPrice(
            origin = TimeRange(startTime = inTime, endTime = outTime), parkTime = DateUtil.diffMins(
                inTime,
                outTime
            ), dailySplits = null
        )

        val service = TimeRange()
        if (retPrice.parkTime <= calcData.cgBasic.serviceTime!!) {
            // BasicPrice(orgTotalPrice = 0, totalPrice = 0, discountPrice = 0, dayilyMaxDiscount = 0)
            service.startTime = inTime
            service.endTime = outTime
            return retPrice
        } else {
            service.startTime = inTime
            service.endTime = DateUtil.getAddMinutes(service.startTime!!, calcData.cgBasic.serviceTime!!.toLong())
        }
        retPrice.service = service

        // 기본요금 타임 적용
        // 서비스 타임 endtime 으로 조회
        if (retPrice.dailySplits == null) {
            logger.debug { "basic fare is null" }
            calcData.getBizHourInfoForDateTime(DateUtil.LocalDateTimeToDateString(inTime), DateUtil.getHourMinuteByLocalDateTime(inTime), vehicleType).let {
                val basic = getBasicFare(it.basicFare!!)
                val basicTime = TimeRange()
//                basicTime.startTime = retPrice.service!!.endTime
                basicTime.startTime = inTime //retPrice.service!!.endTime
                basicTime.endTime = if (DateUtil.getAddMinutes(basicTime.startTime!!, basic.toLong()) > retPrice.origin?.endTime) retPrice.origin?.endTime else DateUtil.getAddMinutes(
                    basicTime.startTime!!,
                    basic.toLong()
                )
                retPrice.basic = basicTime
                retPrice.basicFare = it.basicFare

                //정기권 기간 체크
                val seasonTicket = getSeasonTicket(vehicleNo!!, basicTime.startTime!!, basicTime.endTime!!)
                val dailySplit = DailySplit(
                    startTime = basicTime.startTime!!, endTime = basicTime.endTime!!,
                    payStartTime = basicTime.startTime!!, payEndTime = basicTime.endTime!!,
                    fareInfo = it.basicFare, date = DateUtil.LocalDateTimeToDateString(basicTime.startTime!!),
                    week = DateUtil.getWeek(DateUtil.formatDateTime(basicTime.startTime!!, "yyyy-MM-dd")),
                    priceType = if (seasonTicket != null) "SeasonTicket" else "Normal"
                )
                val dailySplits = ArrayList<DailySplit>()
                dailySplits.add(dailySplit)
                retPrice.dailySplits = dailySplits

                if (seasonTicket != null) {
                    if (seasonTicket.startTime!! <= basicTime.startTime && seasonTicket.endTime!! >= basicTime.endTime) {
                    } else {
                        dailySplits.add(DailySplit(
                            startTime = basicTime.startTime!!, endTime = basicTime.endTime!!,
                            payStartTime = basicTime.startTime!!, payEndTime = basicTime.endTime!!,
                            fareInfo = it.basicFare, date = DateUtil.LocalDateTimeToDateString(basicTime.startTime!!),
                            week = DateUtil.getWeek(DateUtil.formatDateTime(basicTime.startTime!!, "yyyy-MM-dd")),
                            priceType = "Normal"
                        ))
                    }
                }
            }
        }

        if (retPrice.basic!!.endTime!! <= retPrice.origin?.endTime) {
            var startTime = retPrice.basic!!.endTime!!
            do {
                val seasonTicket = getSeasonTicket(vehicleNo!!, startTime, outTime)

                val dailySplit = DailySplit(
                    date = DateUtil.LocalDateTimeToDateString(startTime),
                    week = DateUtil.getWeek(DateUtil.formatDateTime(startTime, "yyyy-MM-dd")),
                    startTime = startTime, endTime = outTime
                )

                if (holidayService.isHolidayByDay(dailySplit.startTime.toLocalDate())) {
                    dailySplit.dateType = "Holiday"
                } else {

                    calcData.getBizHourInfoForDateTime(
                        dailySplit.date,
                        DateUtil.getHourMinuteByLocalDateTime(dailySplit.startTime),
                        vehicleType
                    ).let {
                        // todo 앞요금 / 뒷요금
                        val endTime = if (seasonTicket != null && seasonTicket.startTime!! > dailySplit.startTime)
                                        seasonTicket.startTime!!
                                      else if (dailySplit.date == DateUtil.LocalDateTimeToDateString(outTime)) {
                                        outTime
                                      } else {
                                              DateUtil.beginTimeToLocalDateTime(
                                                  DateUtil.LocalDateTimeToDateString(
                                                      DateUtil.getAddDays(
                                                          dailySplit.startTime,
                                                          1
                                                      )
                                                  )
                                              )
                                      }
                        if (seasonTicket != null && seasonTicket.startTime!! <= dailySplit.startTime) {
                            dailySplit.endTime = DateUtil.getAddSeconds(seasonTicket.endTime!!, 1)
                            dailySplit.priceType = "SeasonTicket"
                        } else {
                            dailySplit.endTime =
                                if (betweenTimeRange(
                                        it.startTime!!, it.endTime!!, DateUtil.getHourMinuteByLocalDateTime(
                                            endTime
                                        )
                                    )) {
                                    endTime
                                } else {
                                    DateUtil.makeLocalDateTime(
                                        dailySplit.date,
                                        it.endTime!!.substring(0, 2),
                                        it.endTime!!.substring(2, 4)
                                    )
                                }
                            val count = DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!) fmod it.addFare!!.time1!!
                            if (count > 0) {
                                dailySplit.endTime = DateUtil.getAddMinutes(
                                    dailySplit.endTime!!,
                                    (it.addFare!!.time1!! - count).toLong()
                                )
                            }
                            logger.debug { "diff mins ${DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!)} mod $count" }
                        }
                        dailySplit.fareInfo = it.addFare
                    }
                }
                startTime = dailySplit.endTime!!
                retPrice.dailySplits!!.add(dailySplit)
            } while(startTime < outTime)
        }

        //할인권 제외
        if (inSn != null) {
            discountService.searchInoutDiscount(inSn)?.let { discounts ->
                discounts.forEach { discount ->
                    val discountClass = discountService.getDiscountClassBySn(discount.discountClassSn)
                    if (discountClass.discountApplyType == DiscountApplyType.TIME) {
                        var discountTime = discountClass.unitTime * discount.quantity!!
                        retPrice.dailySplits!!.forEach { dailySplit ->
                            if (dailySplit.priceType == "Normal") {
                                val min = if (dailySplit.discountRange != null)  DateUtil.diffMins(dailySplit.discountRange!!.endTime!!, dailySplit.endTime!!)
                                else DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!)
                                if (min > 0) {
                                    val applyTime = if (min > discountTime) discountTime else min
                                    if (dailySplit.discountRange != null) {
                                        dailySplit.discountRange!!.endTime = DateUtil.getAddMinutes(dailySplit.discountRange!!.endTime!!, applyTime.toLong())
                                    } else {
                                        val disCount = TimeRange(startTime = dailySplit.startTime,
                                            endTime = DateUtil.getMinByDates(DateUtil.getAddMinutes(dailySplit.startTime, applyTime.toLong()), dailySplit.endTime!! ), type = "DiscountTicket")
                                        dailySplit.discountRange = disCount
                                    }
                                    discountTime = discountTime.minus(applyTime)
                                }
                            }
                        }
                        discount.calcYn = DelYn.Y
                        discountService.saveInoutDiscount(discount)
                    }
                }
            }
        }

        val dailyPrices = ArrayList<DailyPrice>()
        for (i in 0..DateUtil.diffDays(inTime, outTime)+1) {
            val dailyData = retPrice.dailySplits!!.filter {
                it.date == DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(inTime, i.toLong()))
            }

            var originPrice = 0
            var totalPrice = 0
            val dailyPrice = DailyPrice(date = DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(inTime, i.toLong())),
                week = DateUtil.getWeek(DateUtil.formatDateTime(DateUtil.getAddDays(inTime, i.toLong()), "yyyy-MM-dd"))
            )
            dailyData.forEach {
                val originMin = DateUtil.diffMins(it.startTime, it.endTime!!)
                var totalMin = DateUtil.diffMins(it.startTime, it.endTime!!)
                if (it.discountRange != null) {
                    totalMin -= DateUtil.diffMins(it.discountRange!!.startTime!!, it.discountRange!!.endTime!!)
                }
                originPrice += if (it.priceType == "Normal") { ceil(originMin.toFloat() / it.fareInfo!!.time1!!).toInt() * it.fareInfo!!.won1!! } else 0
                totalPrice += if (it.priceType == "Normal") { ceil(totalMin.toFloat() / it.fareInfo!!.time1!!).toInt() * it.fareInfo!!.won1!! } else 0
                dailyPrice.parkTime = dailyPrice.parkTime!!.plus(totalMin)

                logger.debug { "pay range start ${it.startTime} end ${it.endTime} type ${it.priceType} min $originMin price $originPrice discount ${originPrice-totalPrice}"}
            }

            dailyPrice.discount = dailyPrice.discount!!.plus(originPrice-totalPrice)
            dailyPrice.originPrice = dailyPrice.originPrice!!.plus(originPrice)
            dailyPrice.price = dailyPrice.price!!.plus(totalPrice)

            if (calcData.cgBasic.dayMaxAmt != null && calcData.cgBasic.dayMaxAmt!! < dailyPrice.price!!) {
                dailyPrice.dayMaxDiscount = dailyPrice.price!! - calcData.cgBasic.dayMaxAmt!!
                dailyPrice.price = calcData.cgBasic.dayMaxAmt!!
            }

            dailyPrices.add(dailyPrice)
        }

        dailyPrices.forEach {
            retPrice.orgTotalPrice = retPrice.orgTotalPrice!!.plus(it.originPrice!!)
            retPrice.totalPrice = retPrice.totalPrice!!.plus(it.price!!)
            retPrice.discountPrice = retPrice.discountPrice!!.plus(it.discount!!)
            retPrice.dayilyMaxDiscount = retPrice.dayilyMaxDiscount!!.plus(it.dayMaxDiscount!!)
        }

        val wonDiscountAmt = applyInoutDiscountWon(inSn ?: -1, retPrice.totalPrice ?: 0, "out")
        retPrice.discountPrice = retPrice.discountPrice!!.plus(wonDiscountAmt)
        retPrice.totalPrice = retPrice.totalPrice!!.minus(wonDiscountAmt)

        logger.info { "-------------------getBasicPayment-------------------" }
        logger.info { retPrice }
        return retPrice
    }

    fun applyInoutDiscountWon(inSn: Long, totalPrice: Int, type: String, discountClasses: ArrayList<ReqAddParkingDiscount>? = null): Int {
        var discountAmt = 0

        // todo 할인 방식 수정
       discountClasses?.let { discounts ->
            discounts.forEach { discount ->
                discountAmt = discountAmt.plus(calcDiscountWonByVariable(discount.discountClassSn, totalPrice, discountAmt))
            }
        }

        discountService.searchInoutDiscount(inSn)?.let { discounts ->
            discounts.forEach { discount ->
                discountAmt = discountAmt.plus(calcDiscountWonByVariable(discount.discountClassSn, totalPrice, discountAmt))
                if (type == "out") {
                    discount.calcYn = DelYn.Y
                    discountService.saveInoutDiscount(discount)
                }
            }
        }

        discountClasses?.let { discounts ->
            discounts.forEach { discount ->
                discountAmt = calcDiscountWonByFix(discount.discountClassSn, totalPrice, discountAmt)
            }
        }

        discountService.searchInoutDiscount(inSn)?.let { discounts ->
            discounts.forEach { discount ->
                discountAmt = calcDiscountWonByFix(discount.discountClassSn, totalPrice, discountAmt)
                if (type == "out") {
                    discount.calcYn = DelYn.Y
                    discountService.saveInoutDiscount(discount)
                }
            }
        }

        return discountAmt
    }

    fun calcDiscountWonByFix(sn: Long, totalPrice: Int, discountAmt: Int): Int {
        var discount = 0

        val discountClass = discountService.getDiscountClassBySn(sn)
        if (discountClass.discountApplyType == DiscountApplyType.WON) {
            if (discountClass.discountApplyRate == DiscountApplyRateType.FIX) {
                if (totalPrice - discountAmt > discountClass.unitTime)
                    discount = totalPrice - discountAmt - discountClass.unitTime
                else
                    discount = totalPrice - discountAmt
            }
        }
        return discount
    }

    fun calcDiscountWonByVariable(sn: Long, totalPrice: Int, discountAmt: Int): Int {
        var discount = 0

        val discountClass = discountService.getDiscountClassBySn(sn)
        if (discountClass.discountApplyType == DiscountApplyType.WON) {
            if (discountClass.discountApplyRate == DiscountApplyRateType.VARIABLE) {
                if (totalPrice - discountAmt > discountClass.unitTime)
                    discount = discountAmt.plus(discountClass.unitTime)
                else
                    discount = discountAmt.plus(totalPrice)
            }
        }

        return discount
    }

    fun getSeasonTicket(vehicleNo: String, startTime: LocalDateTime, endTime: LocalDateTime): TimeRange? {
        logger.debug { "vaildate season ticket $vehicleNo, $startTime $endTime" }
        try {
            productService.getValidProductByVehicleNo(vehicleNo, startTime, endTime)?.let {
                it.ticket?.let { ticketClass ->
                   if (ticketClass.aplyType == TicketAplyType.FULL) {
                       return TimeRange(
                           startTime = if (startTime > it.effectDate) startTime else it.effectDate,
                           endTime = if (endTime > it.expireDate) it.expireDate else endTime,
                           type = "SEASONTICKET"
                       )
                    } else {
                        val effectDate = DateUtil.makeLocalDateTime(
                            DateUtil.LocalDateTimeToDateString(startTime),
                            ticketClass.startTime!!.substring(0, 2), ticketClass.startTime!!.substring(2, 4))
                       val expireDate = if (ticketClass.startTime!! > ticketClass.endTime!!) {
                           DateUtil.makeLocalDateTime(
                               DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(startTime, 1)),
                               ticketClass.endTime!!.substring(0, 2), ticketClass.endTime!!.substring(2, 4))
                       } else DateUtil.makeLocalDateTime(
                           DateUtil.LocalDateTimeToDateString(startTime),
                           ticketClass.endTime!!.substring(0, 2), ticketClass.endTime!!.substring(2, 4))
                       if (startTime > expireDate) return null
                       return TimeRange(
                           startTime = if (startTime > effectDate) startTime else effectDate,
                           endTime = if (endTime > expireDate) expireDate else endTime,
                           type = "SEASONTICKET"
                       )
                   }
                }
                return TimeRange(
                    startTime = if (startTime > it.effectDate) startTime else it.effectDate,
                    endTime = if (endTime > it.expireDate) it.expireDate else endTime,
                    type = "SEASONTICKET"
                )
            }
            return null
        }catch (e: CustomException) {
            logger.error { "getSeasonTicket ${e.message}" }
            return null
        }
    }

    fun getBasicFare(fareInfo: FareInfo): Int {
        return fareInfo.time1!!*fareInfo.count1!!
                // + fareInfo.time2?.let { fareInfo.time2 }?.run { 0 }!!
                // + fareInfo.time3!!+fareInfo.time4!!+fareInfo.time5!!

    }

    fun getAddFareList(fareInfo: FareInfo): List<ParkingFareInfo> {
        val fareList = ArrayList<ParkingFareInfo>()
        fareList.add(ParkingFareInfo(time = fareInfo.time1, won = fareInfo.won1, count = fareInfo.count1))
        fareInfo.time2?.let { fareList.add(
            ParkingFareInfo(
                time = fareInfo.time2,
                won = fareInfo.won2,
                count = fareInfo.count2
            )
        ) }
        return fareList
    }

    fun betweenTimeRange(start: String, end: String, target: String) : Boolean {
        val result =
            if (start > end) {
                if ( start <= target && target < "2400" ) true
                else "0000" <= target && target < end
            } else {
                start <= target && target < end
            }

        return result
    }

    fun getCalcPayment(inTime: LocalDateTime, outTime: LocalDateTime?, vehicleType: VehicleType, vehicleNo: String?,
                       type: Int, discountMin: Int, inSn: Long?, discountClasses: ArrayList<ReqAddParkingDiscount>?): BasicPrice? {
        if (outTime == null) return null

        logger.info { "-------------------getCalcPayment-------------------" }
        logger.info { "입차시간 : $inTime / 출차시간 : $outTime" }
        logger.info { "차종 : $vehicleType / 차량번호 : $vehicleNo" }
        logger.info { "type(0:할인없음, 1:전방시간할인, 2:후방시간할인) : $type / 할인있을 때의 해당 시간 설정 : $discountMin" }
        logger.info { "-------------------getCalcPayment-------------------" }
        val retPrice = BasicPrice(
            origin = TimeRange(startTime = inTime, endTime = outTime), parkTime = DateUtil.diffMins(
                inTime,
                outTime
            ), dailySplits = null
        )

        val service = TimeRange()
        if (retPrice.parkTime <= calcData.cgBasic.serviceTime!!) {
            // BasicPrice(orgTotalPrice = 0, totalPrice = 0, discountPrice = 0, dayilyMaxDiscount = 0)
            service.startTime = inTime
            service.endTime = outTime
            return retPrice
        } else {
            service.startTime = inTime
            service.endTime = DateUtil.getAddMinutes(service.startTime!!, calcData.cgBasic.serviceTime!!.toLong())
        }
        retPrice.service = service

        // 기본요금 타임 적용
        // 서비스 타임 endtime 으로 조회
        if (retPrice.dailySplits == null) {
            logger.debug { "basic fare is null" }
            calcData.getBizHourInfoForDateTime(DateUtil.LocalDateTimeToDateString(inTime), DateUtil.getHourMinuteByLocalDateTime(inTime), vehicleType).let {
                logger.info { "기본 요금제 $inTime ${it.basicFare?.fareName}" }
                val basic = getBasicFare(it.basicFare!!)
                val basicTime = TimeRange()
//                basicTime.startTime = retPrice.service!!.endTime
                basicTime.startTime = inTime //retPrice.service!!.endTime
                basicTime.endTime = if (DateUtil.getAddMinutes(basicTime.startTime!!, basic.toLong()) > retPrice.origin?.endTime) retPrice.origin?.endTime else DateUtil.getAddMinutes(
                    basicTime.startTime!!,
                    basic.toLong()
                )
                retPrice.basic = basicTime
                retPrice.basicFare = it.basicFare

                //정기권 기간 체크
                val seasonTicket = getSeasonTicket(vehicleNo!!, basicTime.startTime!!, basicTime.endTime!!)
                val dailySplit = DailySplit(
                    startTime = basicTime.startTime!!, endTime = basicTime.endTime!!,
                    payStartTime = basicTime.startTime!!, payEndTime = basicTime.endTime!!,
                    fareInfo = it.basicFare, date = DateUtil.LocalDateTimeToDateString(basicTime.startTime!!),
                    week = DateUtil.getWeek(DateUtil.formatDateTime(basicTime.startTime!!, "yyyy-MM-dd")),
                    priceType = if (seasonTicket != null) "SeasonTicket" else "Normal"
                )
                val dailySplits = ArrayList<DailySplit>()
                dailySplits.add(dailySplit)
                retPrice.dailySplits = dailySplits

                if (seasonTicket != null) {
                    if (seasonTicket.startTime!! <= basicTime.startTime && seasonTicket.endTime!! >= basicTime.endTime) {
                    } else {
                        dailySplits.add(DailySplit(
                            startTime = basicTime.startTime!!, endTime = basicTime.endTime!!,
                            payStartTime = basicTime.startTime!!, payEndTime = basicTime.endTime!!,
                            fareInfo = it.basicFare, date = DateUtil.LocalDateTimeToDateString(basicTime.startTime!!),
                            week = DateUtil.getWeek(DateUtil.formatDateTime(basicTime.startTime!!, "yyyy-MM-dd")),
                            priceType = "Normal"
                        ))
                    }
                }
            }
        }

        if (retPrice.basic!!.endTime!! <= retPrice.origin?.endTime) {
            var startTime = retPrice.basic!!.endTime!!
            do {
                val seasonTicket = getSeasonTicket(vehicleNo!!, startTime, outTime)

                val dailySplit = DailySplit(
                    date = DateUtil.LocalDateTimeToDateString(startTime),
                    week = DateUtil.getWeek(DateUtil.formatDateTime(startTime, "yyyy-MM-dd")),
                    startTime = startTime, endTime = outTime
                )

                if (holidayService.isHolidayByDay(dailySplit.startTime.toLocalDate())) {
                    dailySplit.dateType = "Holiday"
                } else {

                    calcData.getBizHourInfoForDateTime(
                        dailySplit.date,
                        DateUtil.getHourMinuteByLocalDateTime(dailySplit.startTime),
                        vehicleType
                    ).let {
                        logger.info { "요금제 ${dailySplit.date} ${it.addFare?.fareName}" }
                        // todo 앞요금 / 뒷요금
                        val endTime = if (seasonTicket != null && seasonTicket.startTime!! > dailySplit.startTime)
                            seasonTicket.startTime!!
                        else if (dailySplit.date == DateUtil.LocalDateTimeToDateString(outTime)) {
                            outTime
                        } else {
                            DateUtil.beginTimeToLocalDateTime(
                                DateUtil.LocalDateTimeToDateString(
                                    DateUtil.getAddDays(
                                        dailySplit.startTime,
                                        1
                                    )
                                )
                            )
                        }
                        if (seasonTicket != null && seasonTicket.startTime!! <= dailySplit.startTime) {
                            dailySplit.endTime = DateUtil.getAddSeconds(seasonTicket.endTime!!, 1)
                            dailySplit.priceType = "SeasonTicket"
                        } else {
                            dailySplit.endTime =
                                if (betweenTimeRange(
                                        it.startTime!!, it.endTime!!, DateUtil.getHourMinuteByLocalDateTime(
                                            endTime
                                        )
                                    )) {
                                    endTime
                                } else {
                                    DateUtil.makeLocalDateTime(
                                        dailySplit.date,
                                        it.endTime!!.substring(0, 2),
                                        it.endTime!!.substring(2, 4)
                                    )
                                }
                            val count = DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!) fmod it.addFare!!.time1!!
                            if (count > 0) {
                                dailySplit.endTime = DateUtil.getAddMinutes(
                                    dailySplit.endTime!!,
                                    (it.addFare!!.time1!! - count).toLong()
                                )
                            }
                            logger.debug { "diff mins ${DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!)} mod $count" }
                        }
                        dailySplit.fareInfo = it.addFare
                    }
                }
                startTime = dailySplit.endTime!!
                retPrice.dailySplits!!.add(dailySplit)
            } while(startTime < outTime)
        }

        //할인권 제외
        if (inSn != null) {
            discountClasses?.let { discounts ->
                discounts.forEach { discount ->
                    val discountClass = discountService.getDiscountClassBySn(discount.discountClassSn)
                    var discountTime = discountClass.unitTime * discount.cnt
                    retPrice.dailySplits!!.forEach { dailySplit ->
                        if (dailySplit.priceType == "Normal" && discountClass.discountApplyType == DiscountApplyType.TIME) {
                            val min = if (dailySplit.discountRange != null)  DateUtil.diffMins(dailySplit.discountRange!!.endTime!!, dailySplit.endTime!!)
                            else DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!)
                            if (min > 0) {
                                val applyTime = if (min > discountTime) discountTime else min
                                if (dailySplit.discountRange != null) {
                                    dailySplit.discountRange!!.endTime = DateUtil.getAddMinutes(dailySplit.discountRange!!.endTime!!, applyTime.toLong())
                                } else {
                                    val discount = TimeRange(startTime = dailySplit.startTime,
                                        endTime = DateUtil.getMinByDates(DateUtil.getAddMinutes(dailySplit.startTime, applyTime.toLong()), dailySplit.endTime!! ), type = "DiscountTicket")
                                    dailySplit.discountRange = discount
                                }

                                discountTime = discountTime.minus(applyTime)
                            }
                        }
                    }
//                    discount.calcYn = DelYn.Y
//                    discountService.saveInoutDiscount(discount)
                }
            }
        }

        val dailyPrices = ArrayList<DailyPrice>()
        for (i in 0..DateUtil.diffDays(inTime, outTime)+1) {
            val dailyData = retPrice.dailySplits!!.filter {
                it.date == DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(inTime, i.toLong()))
            }

            var originPrice = 0
            var totalPrice = 0
            val dailyPrice = DailyPrice(date = DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(inTime, i.toLong())),
                week = DateUtil.getWeek(DateUtil.formatDateTime(DateUtil.getAddDays(inTime, i.toLong()), "yyyy-MM-dd"))
            )
            dailyData.forEach {
                val originMin = DateUtil.diffMins(it.startTime, it.endTime!!)
                var totalMin = DateUtil.diffMins(it.startTime, it.endTime!!)
                if (it.discountRange != null) {
                    totalMin -= DateUtil.diffMins(it.discountRange!!.startTime!!, it.discountRange!!.endTime!!)
                }
                originPrice += if (it.priceType == "Normal") { ceil(originMin.toFloat() / it.fareInfo!!.time1!!).toInt() * it.fareInfo!!.won1!! } else 0
                totalPrice += if (it.priceType == "Normal") { ceil(totalMin.toFloat() / it.fareInfo!!.time1!!).toInt() * it.fareInfo!!.won1!! } else 0
                dailyPrice.parkTime = dailyPrice.parkTime!!.plus(totalMin)

                logger.debug { "pay range start ${it.startTime} end ${it.endTime} type ${it.priceType} min $originMin price $originPrice discount ${originPrice-totalPrice}"}
            }

            dailyPrice.discount = dailyPrice.discount!!.plus(originPrice-totalPrice)
            dailyPrice.originPrice = dailyPrice.originPrice!!.plus(originPrice)
            dailyPrice.price = dailyPrice.price!!.plus(totalPrice)

            if (calcData.cgBasic.dayMaxAmt != null && calcData.cgBasic.dayMaxAmt!! < dailyPrice.price!!) {
                dailyPrice.dayMaxDiscount = dailyPrice.price!! - calcData.cgBasic.dayMaxAmt!!
                dailyPrice.price = calcData.cgBasic.dayMaxAmt!!
            }

            dailyPrices.add(dailyPrice)
        }

        dailyPrices.forEach {
            retPrice.orgTotalPrice = retPrice.orgTotalPrice!!.plus(it.originPrice!!)
            retPrice.totalPrice = retPrice.totalPrice!!.plus(it.price!!)
            retPrice.discountPrice = retPrice.discountPrice!!.plus(it.discount!!)
            retPrice.dayilyMaxDiscount = retPrice.dayilyMaxDiscount!!.plus(it.dayMaxDiscount!!)
        }

        val wonDiscountAmt = applyInoutDiscountWon(inSn ?: -1, retPrice.totalPrice ?: 0, "calc", discountClasses)
        retPrice.discountPrice = retPrice.discountPrice!!.plus(wonDiscountAmt)
        retPrice.totalPrice = retPrice.totalPrice!!.minus(wonDiscountAmt)

        logger.info { "-------------------getBasicPayment-------------------" }
        logger.info { retPrice }
        return retPrice
    }
}

inline infix fun <reified T : Number> T.fmod(other: T): T {
    return when {
        this is BigDecimal || other is BigDecimal -> BigDecimal(other.toString()).let {
            (((BigDecimal(this.toString()) % it) + it) % it) as T
        }
        this is BigInteger || other is BigInteger -> BigInteger(other.toString()).let {
            (((BigInteger(this.toString()) % it) + it) % it) as T
        }
        this is Double || other is Double -> other.toDouble().let {
            (((this.toDouble() % it) + it) % it) as T
        }
        this is Float || other is Float -> other.toFloat().let {
            (((this.toFloat() % it) + it) % it) as T
        }
        this is Long || other is Long -> other.toLong().let {
            (((this.toLong() % it) + it) % it) as T
        }
        this is Int || other is Int -> other.toInt().let {
            (((this.toInt() % it) + it) % it) as T
        }
        this is Short || other is Short -> other.toShort().let {
            (((this.toShort() % it) + it) % it) as T
        }
        else -> throw AssertionError()
    }
}