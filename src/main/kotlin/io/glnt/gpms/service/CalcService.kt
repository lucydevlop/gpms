package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.calc.CalculationData
import io.glnt.gpms.handler.calc.model.BasicPrice
import io.glnt.gpms.handler.calc.model.FareItem
import io.glnt.gpms.handler.calc.model.FareRange
import io.glnt.gpms.handler.calc.model.TimeRange
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.model.dto.DiscountApplyDTO
import io.glnt.gpms.model.dto.request.ReqAddParkingDiscount
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.enums.DiscountRangeType
import io.glnt.gpms.model.enums.TicketAplyType
import io.glnt.gpms.model.enums.VehicleType
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CalcService(
    private var calcData: CalculationData,
    private var ticketService: TicketService
) {
    companion object : KLogging()

    /**
     * @param inTime 입차시간
     * @param outTime 출차시간
     * @param vehicleType 차종
     * @param vehicleNo 차번호
     * @param type  0 : 할인없음, 1 : 전방시간할인, 2 : 후방시간 할인 - DiscountApplyDTO
     * @param discountMin 할인이 있을때 해당시간 설정, 없으면 0으로 호출(회차시간)
     * @param inSn 입차seq
     * @param discountClasses 적용하지 않은
     * @return 요금정보관련클래스
     */
    fun calcParkinglotFee(inTime: LocalDateTime, outTime: LocalDateTime?, vehicleType: VehicleType, vehicleNo: String,
                          //type: DiscountApplyDTO?,
                          discountMin: Int,
                          inSn: Long?, discountClasses: ArrayList<ReqAddParkingDiscount>?, isReCharge: Boolean): BasicPrice? {
        if (outTime == null) return null
        FeeCalculation.logger.info { "-------------------getCalcPayment-------------------" }
        FeeCalculation.logger.info { "입차시간 : $inTime / 출차시간 : $outTime" }
        FeeCalculation.logger.info { "차종 : $vehicleType / 차량번호 : $vehicleNo / 입차seq : $inSn" }
        FeeCalculation.logger.info { "type(0:할인없음, 1:전방시간할인, 2:후방시간할인) : / 할인있을 때의 해당 시간 설정(회차시간) : $discountMin" }
        FeeCalculation.logger.info { "-------------------getCalcPayment-------------------" }

        val retPrice = BasicPrice(
            origin = TimeRange(startTime = inTime, endTime = outTime),
            parkTime = DateUtil.diffMins(inTime, outTime),
            dailySplits = null,
            totalPrice = 0,
            orgTotalPrice = 0
        )

        retPrice.serviceTime = if (isReCharge) calcData.cgBasic.legTime?: 0 else calcData.cgBasic.serviceTime?: 0
        // 회차(레그) 타임 check
        if (retPrice.parkTime <= retPrice.serviceTime) {
            retPrice.service = TimeRange(startTime = inTime, endTime = outTime)
            return retPrice
        } else {
            retPrice.service = TimeRange(startTime = inTime, endTime = DateUtil.getAddMinutes(inTime, retPrice.serviceTime.toLong()))
        }

        // 요금구간 세팅
        // step1. 기본요금
        calcData.getBizHourInfoForDateTime(DateUtil.LocalDateTimeToDateString(inTime), DateUtil.getHourMinuteByLocalDateTime(inTime), vehicleType).let { farePolicy ->
            farePolicy.basicFare?.let { basicFare ->
                logger.info { "기본 요금제 $inTime ${basicFare.fareName}" }
                getFareTimes(basicFare).let { times ->
                    val basicTime = times.sumOf { (it.time?: 1) * (it.count?: 1) }
                    retPrice.basicRange = TimeRange(
                        startTime = inTime,
                        endTime = if (DateUtil.getAddMinutes(inTime, basicTime.toLong()) > retPrice.origin?.endTime)
                                    retPrice.origin?.endTime
                                  else DateUtil.getAddMinutes(inTime, basicTime.toLong()
                        ))
                    retPrice.basicFare = basicFare
                    var fareTime = 0
                    times.forEach { time ->
                        val apply = fareTime + ( (time.time?: 0) * (time.count?: 0))
                        retPrice.fareRanges?.add(FareRange(startTime = DateUtil.getAddMinutes(inTime, fareTime.toLong()), endTime = DateUtil.getAddMinutes(inTime, apply.toLong()), won = time.won))
                        fareTime = apply
                    }
                }
            }
        }

        // step2. 추가요금
        if (retPrice.basicRange!!.endTime!! <= retPrice.origin?.endTime) {
            var startTime = retPrice.basicRange!!.endTime!!
            do {
                calcData.getBizHourInfoForDateTime(DateUtil.LocalDateTimeToDateString(startTime), DateUtil.getHourMinuteByLocalDateTime(startTime), vehicleType).let { farePolicy ->
                    farePolicy.addFare?.let { addFare ->
                        logger.info { "추가 요금제 $inTime ${addFare.fareName}" }
                        getFareTimes(addFare).let { times ->
                            val basicTime = times.sumOf { (it.time?: 1) * (it.count?: 1) }
                            retPrice.addRange?.add(TimeRange(
                                startTime = startTime,
                                endTime = if (DateUtil.getAddMinutes(startTime, basicTime.toLong()) > retPrice.origin?.endTime)
                                    retPrice.origin?.endTime
                                else DateUtil.getAddMinutes(startTime, basicTime.toLong())))
                            var fareTime = 0
                            times.forEach { time ->
                                val apply = fareTime + ( (time.time?: 0) * (time.count?: 0))
                                retPrice.fareRanges?.add(FareRange(startTime = DateUtil.getAddMinutes(startTime, fareTime.toLong()), endTime = DateUtil.getAddMinutes(startTime, apply.toLong()), won = time.won))
                                fareTime = apply
                            }
                            startTime = DateUtil.getAddMinutes(startTime, fareTime.toLong())
                        }
                    }
                }
            } while(startTime < outTime)
        }

        val seasonTickets = getSeasonTicketRange(vehicleNo, inTime, outTime)

        // step3. 정기권 구간 check


        //시간 구간에 따라
        return retPrice
    }

    private fun getFareTimes(fareInfo: FareInfo): ArrayList<FareItem> {
        val times = ArrayList<FareItem>()
        fareInfo.time1?.let { time ->
            times.add(FareItem(time = time, won = (fareInfo.won1?: 1), count = (fareInfo.count1?: 1)))
        }
        fareInfo.time2?.let { time ->
            times.add(FareItem(time = time, won = (fareInfo.won2?: 1), count = (fareInfo.count2?: 1)))
        }
        fareInfo.time3?.let { time ->
            times.add(FareItem(time = time, won = (fareInfo.won3?: 1), count = (fareInfo.count3?: 1)))
        }
        fareInfo.time4?.let { time ->
            times.add(FareItem(time = time, won = (fareInfo.won4?: 1), count = (fareInfo.count4?: 1)))
        }
        return times
    }

    private fun getSeasonTicketRange(vehicleNo: String, startTime: LocalDateTime, endTime: LocalDateTime): List<TimeRange>? {
        val timeRanges = ArrayList<TimeRange>()
        ticketService.getTicketByVehicleNoAndDate(vehicleNo, startTime, endTime)?.let { tickets ->
            tickets.forEach { productTicketDTO ->
                productTicketDTO.ticket?.let { ticketClassDTO ->
                    when(ticketClassDTO.rangeType) {
                        DiscountRangeType.ALL -> {
                            if (ticketClassDTO.aplyType == TicketAplyType.FULL) {
                                timeRanges.add(TimeRange(startTime = if (startTime > productTicketDTO.effectDate) startTime else productTicketDTO.effectDate,
                                                          endTime = if (endTime > productTicketDTO.expireDate) productTicketDTO.expireDate else endTime,
                                                          type = "SEASONTICKET"))
                            } else {
                                val effectDate = DateUtil.makeLocalDateTime(
                                    DateUtil.LocalDateTimeToDateString(startTime),
                                    ticketClassDTO.startTime!!.substring(0, 2), ticketClassDTO.startTime!!.substring(2, 4))
                                val expireDate = if (ticketClassDTO.startTime!! > ticketClassDTO.endTime!!) {
                                    DateUtil.makeLocalDateTime(
                                        DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(startTime, 1)),
                                        ticketClassDTO.endTime!!.substring(0, 2), ticketClassDTO.endTime!!.substring(2, 4))
                                } else DateUtil.makeLocalDateTime(
                                    DateUtil.LocalDateTimeToDateString(startTime),
                                    ticketClassDTO.endTime!!.substring(0, 2), ticketClassDTO.endTime!!.substring(2, 4))
                                if (startTime > expireDate) {
                                } else {
                                    timeRanges.add(TimeRange(startTime = if (startTime > productTicketDTO.effectDate) startTime else productTicketDTO.effectDate,
                                                             endTime = if (endTime > productTicketDTO.expireDate) productTicketDTO.expireDate else endTime,
                                                             type = "SEASONTICKET"))
                                }
                            }
                        }
                        DiscountRangeType.WEEKDAY -> {
                            val targetDay = startTime
                            while(DateUtil.LocalDateTimeToDateString(targetDay) <= DateUtil.LocalDateTimeToDateString(endTime)) {
                                if (ticketClassDTO.aplyType == TicketAplyType.FULL) {
                                    timeRanges.add(
                                        TimeRange(startTime = if (targetDay > productTicketDTO.effectDate) targetDay else productTicketDTO.effectDate,
                                                  endTime = if (endTime > productTicketDTO.expireDate) productTicketDTO.expireDate else endTime,
                                                  type = "SEASONTICKET"))
                                }

                                targetDay.plusDays(1)
                            }

                        }
                    }
                }
            }
            return timeRanges

        }?: run { return null }
    }
}