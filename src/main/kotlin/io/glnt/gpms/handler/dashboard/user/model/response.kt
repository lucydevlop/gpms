package io.glnt.gpms.handler.dashboard.user.model

import io.glnt.gpms.model.enums.YN
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
    var calcYn: YN,
    var delYn: YN,
    var createDate: LocalDateTime,
    var quantity: Int,
    var ticketClassSn: Long? = null
)