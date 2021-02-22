package io.glnt.gpms.handler.discount.model

import java.time.LocalDateTime

data class reqSearchDiscount(
    var corpId: String? = null
)

data class reqDiscountableTicket(
    var corpId: String,
    var date: LocalDateTime
)
