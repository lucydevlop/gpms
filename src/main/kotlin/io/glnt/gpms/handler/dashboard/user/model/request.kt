package io.glnt.gpms.handler.dashboard.user.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.TicketType
import java.time.LocalDate
import java.time.LocalDateTime

data class reqVehicleSearch(
    var vehicleNo: String,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") var inDate: LocalDateTime? = null
)

data class reqParkingDiscounTicketSearch(
    var vehicleNo: String,
    var inDate: LocalDateTime
)

data class reqParkingDiscountAbleTicketsSearch(
    var inSn: Long? = null,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") var inDate: LocalDateTime? = null,
    var corpSn: Long
)

data class reqParkingDiscountAddTicket(
    var inSn: Long,
    var corpSn: Long,
    var discountClassSn: Long,
    var cnt: Int
)

data class reqParkingDiscountApplyTicketSearch(
    var corpSn: Long,
    @JsonFormat(pattern="yyyy-MM-dd") var startDate: LocalDate,
    @JsonFormat(pattern="yyyy-MM-dd") var endDate: LocalDate,
    var ticketType: TicketType? = TicketType.ALL,
    var applyStatus: String?,
    var vehicleNo: String?


)