package io.glnt.gpms.handler.calc.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.CalculationData
import io.glnt.gpms.handler.calc.model.*
import io.glnt.gpms.handler.holiday.service.HolidayService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.enums.VehicleType
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime


@Service
class FeeCalculation {
    companion object : KLogging()

    @Autowired
    private lateinit var holidayService: HolidayService

    @Autowired
    private lateinit var productService: ProductService

    @Autowired
    private lateinit var calcData: CalculationData

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
        type: Int, discountMin: Int
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
            calcData.getBizHourInfoForDateTime(
                DateUtil.LocalDateTimeToDateString(retPrice.service!!.endTime!!), DateUtil.getHourMinuteByLocalDateTime(
                    retPrice.service!!.endTime!!
                ), vehicleType
            ).let {
                val basic = getBasicFare(it.basicFare!!)
                val basicTime = TimeRange()
                basicTime.startTime = retPrice.service!!.endTime
                basicTime.endTime = if (DateUtil.getAddMinutes(basicTime.startTime!!, basic.toLong()) > retPrice.origin.endTime) retPrice.origin.endTime else DateUtil.getAddMinutes(
                    basicTime.startTime!!,
                    basic.toLong()
                )
                retPrice.basic = basicTime
                retPrice.basicFare = it.basicFare
                val dailySplit = DailySplit(
                    startTime = basicTime.startTime!!, endTime = basicTime.endTime!!,
                    payStartTime = basicTime.startTime!!, payEndTime = basicTime.endTime!!,
                    fareInfo = it.basicFare, date = DateUtil.LocalDateTimeToDateString(basicTime.startTime!!),
                    week = DateUtil.getWeek(DateUtil.formatDateTime(basicTime.startTime!!, "yyyy-MM-dd"))
                )
                val dailySplits = ArrayList<DailySplit>()
                dailySplits.add(dailySplit)
                retPrice.dailySplits = dailySplits
            }
        }

        if (retPrice.basic!!.endTime!! < retPrice.origin.endTime) {
            var startTime = retPrice.basic!!.endTime!!
            do {
                val seasonTicket = getSeasonTicket(vehicleNo!!, startTime, outTime)

                val dailySplit = DailySplit(
                    date = DateUtil.LocalDateTimeToDateString(startTime),
                    week = DateUtil.getWeek(DateUtil.formatDateTime(startTime, "yyyy-MM-dd")),
                    startTime = startTime, endTime = outTime
                )

                if (holidayService.isHolidayByLocalDateTime(dailySplit.startTime)) {
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
//                            if (DateUtil.getHourMinuteByLocalDateTime(seasonTicket.endTime!!)=="2359")
                                dailySplit.endTime = DateUtil.getAddSeconds(seasonTicket.endTime!!, 1)
//                            else
//                                dailySplit.endTime = seasonTicket.endTime
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
//                            dailySplit.endTime = if (it.startTime!! < it.endTime!!) {
//                                if (DateUtil.getHourMinuteByLocalDateTime(endTime) == "0000")
//                                    DateUtil.makeLocalDateTime(dailySplit.date, it.endTime!!.substring(0, 2), it.endTime!!.substring(2, 4))
//                                else {
//                                    if (DateUtil.getHourMinuteByLocalDateTime(endTime) >= it.endTime!!)
//                                        DateUtil.makeLocalDateTime(
//                                            dailySplit.date,
//                                            it.endTime!!.substring(0, 2),
//                                            it.endTime!!.substring(2, 4)
//                                        )
//                                    else
//                                        endTime
//                                }
//                            } else {
////                                if (DateUtil.getHourMinuteByLocalDateTime(endTime) == "0000")
////                                    endTime
////                                else {
//                                    if (DateUtil.getHourMinuteByLocalDateTime(endTime) >= it.endTime!!)
//                                        endTime
//                                    else
//                                        DateUtil.makeLocalDateTime(dailySplit.date, it.endTime!!.substring(0, 2), it.endTime!!.substring(2, 4))
////                                }
//                            }

                            val count = DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!) fmod it.addFare!!.time1!!
                            if (count > 0) {
                                dailySplit.endTime = DateUtil.getAddMinutes(
                                    dailySplit.endTime!!,
                                    (it.addFare!!.time1!! - count).toLong()
                                )
                            }
                            logger.debug { "diff mins $DateUtil.diffMins(dailySplit.startTime, dailySplit.endTime!!) mod $count" }
                        }
                        dailySplit.fareInfo = it.addFare
                    }
                }
                startTime = dailySplit.endTime!!
                retPrice.dailySplits!!.add(dailySplit)
            } while(startTime < outTime)
        }

        for (i in 0 until DateUtil.diffDays(inTime, outTime)+1) {
            val dailyData = retPrice.dailySplits!!.filter {
                it.date == DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(inTime, i.toLong()))
            }

            var price = 0
            dailyData.forEach {
                val min = DateUtil.diffMins(it.startTime, it.endTime!!)
                price += if (it.priceType == "Normal") min / it.fareInfo!!.time1!! * it.fareInfo!!.won1!! else 0
                logger.debug { "pay range start ${it.startTime} end ${it.endTime} type ${it.priceType} min $min price $price"}
            }

            if (calcData.cgBasic.dayMaxAmt!! in 1 until price) {
                retPrice.dayilyMaxDiscount = retPrice.dayilyMaxDiscount?.plus(price-calcData.cgBasic.dayMaxAmt!!)
                retPrice.totalPrice = retPrice.totalPrice?.plus(calcData.cgBasic.dayMaxAmt!!)
            } else {
                retPrice.totalPrice = retPrice.totalPrice?.plus(price)
            }
            retPrice.orgTotalPrice = retPrice.orgTotalPrice?.plus(price)

        }
        return retPrice
    }

    fun getSeasonTicket(vehicleNo: String, startTime: LocalDateTime, endTime: LocalDateTime): TimeRange? {
        logger.info { "vaildate season ticket $vehicleNo, $startTime $endTime" }
        try {
            productService.getValidProductByVehicleNo(vehicleNo, startTime, endTime)?.let {
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