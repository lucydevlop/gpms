package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.calc.CalculationData
import io.glnt.gpms.handler.calc.model.BasicPrice
import io.glnt.gpms.handler.calc.model.TimeRange
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.model.dto.DiscountApplyDTO
import io.glnt.gpms.model.dto.request.ReqAddParkingDiscount
import io.glnt.gpms.model.enums.DiscountRangeType
import io.glnt.gpms.model.enums.TicketAplyType
import io.glnt.gpms.model.enums.VehicleType
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class CalcService(
    private var calcData: CalculationData,
    private var ticketService: TicketService
) {

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
    fun calcParkinglotFee(inTime: LocalDateTime, outTime: LocalDateTime?,
                          vehicleType: VehicleType, vehicleNo: String,
                          type: DiscountApplyDTO?, discountMin: Int,
                          inSn: Long?, discountClasses: ArrayList<ReqAddParkingDiscount>?): BasicPrice? {
        if (outTime == null) return null
        FeeCalculation.logger.info { "-------------------getCalcPayment-------------------" }
        FeeCalculation.logger.info { "입차시간 : $inTime / 출차시간 : $outTime" }
        FeeCalculation.logger.info { "차종 : $vehicleType / 차량번호 : $vehicleNo / 입차seq : $inSn" }
        FeeCalculation.logger.info { "type(0:할인없음, 1:전방시간할인, 2:후방시간할인) : $type / 할인있을 때의 해당 시간 설정(회차시간) : $discountMin" }
        FeeCalculation.logger.info { "-------------------getCalcPayment-------------------" }

        val retPrice = BasicPrice(
            origin = TimeRange(startTime = inTime, endTime = outTime),
            parkTime = DateUtil.diffMins(inTime, outTime),
            dailySplits = null
        )

        //회차 데이터 check
        if (retPrice.parkTime <= calcData.cgBasic.serviceTime!!) {
            val service = TimeRange()
            // BasicPrice(orgTotalPrice = 0, totalPrice = 0, discountPrice = 0, dayilyMaxDiscount = 0)
            service.startTime = inTime
            service.endTime = outTime
            return retPrice
        }

        val seasonTickets = getSeasonTicketRange(vehicleNo, inTime, outTime)

        //기본요금 적용
        calcData.getBizHourInfoForDateTime(DateUtil.LocalDateTimeToDateString(inTime), DateUtil.getHourMinuteByLocalDateTime(inTime), vehicleType).let { farePolicy ->
            farePolicy.basicFare?.let { basicFare ->
                val time = ((basicFare.time1 ?: 0) * (basicFare.count1?: 0))
                val basic = TimeRange()
                basic.startTime = inTime
                basic.endTime = if (DateUtil.getAddMinutes(inTime, time.toLong()) > retPrice.origin?.endTime)
                                    retPrice.origin?.endTime
                                else DateUtil.getAddMinutes(inTime, time.toLong())
                retPrice.basic = basic
                retPrice.basicFare = basicFare
            }
        }


        while(true){

        }

        //시간 구간에 따라
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