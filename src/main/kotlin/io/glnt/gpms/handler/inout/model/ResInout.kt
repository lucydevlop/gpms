package io.glnt.gpms.handler.inout.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DisplayMessageClass
import java.time.LocalDateTime

data class resParkInList(
    var type: DisplayMessageClass,
    var parkinSn: Long? = null,
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
    var discountfee: Int? = 0,
    /* 입차 차량이미지 File (base64) */
    var inImgBase64Str: String? = null,
    /* 출차 차량이미지 File (base64) */
    var outImgBase64Str: String? = null,
    var ticketCorpName: String? = null,
    var memo: String? = null,
    var paymentAmount: Int? = 0
)
