package io.glnt.gpms.handler.inout.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DisplayMessageClass
import java.time.LocalDateTime

data class ResParkInList(
    var type: DisplayMessageClass,
    var parkinSn: Long,
    var vehicleNo: String? = null,
    var parkcartype: String,
    var inGateId: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") var inDate: LocalDateTime,
    var parkoutSn: Long? = null,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") var outDate: LocalDateTime? = null,
    var outGateId: String? = null,
    var parktime: Int? = 0,
    var parkfee: Int? = 0,
    var payfee: Int? = 0,
    var discountfee: Int? = 0
)
