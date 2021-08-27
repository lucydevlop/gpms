package io.glnt.gpms.handler.discount.model

import io.glnt.gpms.model.enums.TicketType
import java.time.LocalDate
import java.time.LocalDateTime

data class reqDiscountableTicket(
    var corpSn: Long,
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

data class reqAddInoutDiscount(
    var inSn: Long,
    var ticketSn: Long? = null,
    var discountType: TicketType,
    var quantity: Int? = 1,
    var discountClassSn: Long,
    var corpSn: Long? = null,
    var ticketClassSn: Long? = null
)

data class reqApplyInoutDiscountSearch(
    var ticketSn: Long,
    var startDate: LocalDate,
    var endDate: LocalDate,
    //var ticketType: TicketType,
    var applyStatus: String?,
    var ticketsSn: List<Long>? = null
)
