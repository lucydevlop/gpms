package io.glnt.gpms.handler.vehicle.model

import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime

data class ResParkInList(
    var parkinSn: Long,
    var vehicleNo: String? = null,
    var parkcartype: String,
    var inGateId: String? = null,

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") var inDate: LocalDateTime,
    var parkoutSn: Long? = null,
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss") var outDate: LocalDateTime? = null,
    var outGateId: String? = null,
    var parktime: Int? = 0,
    var parkfee: Int? = 0,
    var payfee: Int? = 0,
    var discountfee: Int? = 0
)
