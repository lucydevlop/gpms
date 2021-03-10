package io.glnt.gpms.handler.discount.model

import java.time.LocalDateTime

data class reqSearchDiscount(
    var corpId: String? = null
)

data class reqDiscountableTicket(
    var corpId: String,
    var date: LocalDateTime?,
    var inSn: Long?
)

data class reqSearchInoutDiscount(
    var ticketSn: Long,
    var inSn: Long
)

data class availableTicketClass(
    var type: String,
    var useCount: Int? = 0,
    var setCount: Int? = 99999
)
