package io.glnt.gpms.handler.calc.model

import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.enums.WeekType
import okhttp3.RequestBody.Companion.asRequestBody
import java.time.LocalDateTime

data class RealPaymentTime(
    var startTime: String? = "",
    var endTime: String? = ""
)

data class BasicPrice(
    //최대 요금 적용 전 총 요금
    var orgTotalPrice: Int? = 0,
    //총가격
    var totalPrice: Int? = 0,
    //할인가격
    var discountPrice: Int? = 0,
    //일 최대가격이 적용된 차감액
    var dayilyMaxDiscount: Int? = 0,
    //절대 최대가격이 적용된 차감액
    var absMaxDiscount: Int? = 0,
    //시간 할인 총액
    var timeDisCounted: Int? = 0,
    //절상 금액
    var cut: Int? = 0,
    // 총 주차시간
    // origin
    var parkTime: Int = 0,
    var origin: TimeRange,
    // 서비스시간
    var serviceTime: Int = 0,
    var service: TimeRange? = null,
    // 요금
    var basic: TimeRange? = null,
    var basicFare: FareInfo? = null,
    // 추가요금
    var add: TimeRange? = null,
    // fare list
    var dailySplits: ArrayList<DailySplit>?
)

data class DailyPrice(
    var originStartTime: LocalDateTime?,
    var originEndTime: LocalDateTime?,
    var date: String,
    var week: WeekType?,
    var dateType: String? = "Normal", //Holiday(휴일), Overday(특근일), Normal(보통일)
    var price: Int? = 0,
    var priceType: String? = "Normal", //payType Month(정기권무료), Month_half(정기권일부할인), Free(무료), Free_half(무료일부할인), Normal(보통결제)
    var startTime: LocalDateTime?,
    var endTime: LocalDateTime?,
    var dailySplits: ArrayList<DailySplit>? = null,
    var serviceTime: Int = 0,
    var diffTime: Int = 0
)

data class DailySplit(
    var date: String,
    var week: WeekType?,
    var dateType: String? = "Normal",
    var priceType: String? = "Normal",
    var startTime: LocalDateTime,
    var endTime: LocalDateTime? = null,
    var payStartTime: LocalDateTime? = null,
    var payEndTime: LocalDateTime? = null,
    var overTime: Int? = 0,
    var fareAmt: Int? = 0,
    var fareInfo: FareInfo? = null,
    var farePolicy: FarePolicy? = null,
    var seasonTicketRange: TimeRange? = null
)

data class TimeRange(
    var startTime: LocalDateTime? = null,
    var endTime: LocalDateTime? = null,
    var type: String? = null
)

data class ParkingFareInfo(
    var time : Int? = 0,
    var won : Int? = 0,
    var count : Int? = 0
)