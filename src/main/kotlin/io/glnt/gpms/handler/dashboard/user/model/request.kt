package io.glnt.gpms.handler.dashboard.user.model

import com.fasterxml.jackson.annotation.JsonFormat
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