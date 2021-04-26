package io.glnt.gpms.model.dto.request

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.enums.*
import java.time.LocalDate
import java.time.LocalDateTime

data class reqCreateProductTicket(
    var sn: Long? = null,
    var vehicleNo: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime,
    var userId: String? = null,
    var gateId: MutableSet<String>? = null,
    var ticketType: TicketType? = null,
    var vehicleType: VehicleType? = null,
    var corpSn: Long? = null,
    var corpName: String? = null,
    var etc: String? = null,
    var name: String? = null,
    var etc1: String? = null,
    var tel: String? = null,
    var vehiclekind: String? = null
)

data class reqSearchProductTicket(
    var searchLabel: String? = null,
    var searchText: String? = null,
    var searchDateLabel: DateType? = DateType.EFFECT,
    @JsonFormat(pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime? = null,
    var ticketType: TicketType? = null,
    var delYn: String? = "N"
)

data class reqDisplayInfo(
    var line1Status: DisplayStatus,
    var line2Status: DisplayStatus
)

data class reqUserInfo(
    var idx: Long? = null,
    var id: String? = null,
    var password: String? = null,
    var userName: String? = null,
    var userPhone: String? = null,
    var role: UserRole? = null

)

data class reqFareInfo(
    var fareName: String,
    var type: FareType? = FareType.BASIC,
    var time1: Int? = 30,
    var won1: Int? = 1000,
    var count1: Int? = 1,
    var count: Int? = 1
)

data class reqFarePolicy(
    var fareName: String,
    var vehicleType: VehicleType? = VehicleType.SMALL,
    var startTime: String? = "0000",
    var endTime: String? = "2359",
    var basicFareSn: Long,
    var addFareSn: Long? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
    var week: MutableSet<String>? = mutableSetOf(WeekType.ALL.toString())
)

data class reqFareBasic(
    var sn: Long? = null,
    var serviceTime: Int? = 0,
    var regTime: Int? = 0,
    var dayMaxAmt: Int? = 0,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss")
)

data class reqDiscountTicket(
    var sn: Long? = null,
    var discountNm: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),
    var dayRange: DiscountRangeType? = DiscountRangeType.ALL,
    var unitTime: Int? = 0,
    var disUse: SaleType? = SaleType.FREE,
    var disMaxNo: Int? = 1,
    var disMaxDay: Int? = 9999,
    var disMaxMonth: Int? = 9999,
    var disPrice: Int? = 0
)