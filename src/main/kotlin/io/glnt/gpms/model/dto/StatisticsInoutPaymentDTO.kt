package io.glnt.gpms.model.dto

import javax.validation.constraints.NotNull

data class StatisticsInoutPaymentDTO(
    @get: NotNull
    var date: String? = null,

    var parkFee: Int? = 0,

    var discountFee : Int? = 0,

    var dayDiscountFee: Int? = 0,

    var payFee: Int? = 0,

    var payment: Int? = 0,

    var nonPayment: Int? = 0
)
