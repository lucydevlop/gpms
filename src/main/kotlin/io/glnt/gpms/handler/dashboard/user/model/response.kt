package io.glnt.gpms.handler.dashboard.user.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import java.time.LocalDateTime

data class resVehicleSearch(
    var sn: Long,
    var vehicleNo: String,
    var inDate: String,
    var imImagePath: String? = null
)

data class ResDiscountTicetsApplyList(
    var sn: Long,
    var discountType: TicketType,
    var vehicleNo: String,
    var discountClassSn: Long,
    var discountNm: String,
    var calcYn: DelYn,
    var delYn: DelYn,
    var createDate: LocalDateTime,
    var quantity: Int
)