package io.glnt.gpms.model.dto

import javax.validation.constraints.NotNull

data class StatisticsInoutByDayDTO(
    @get: NotNull
    var date: String? = null,

    var inCnt: Int? = null,

    var outCnt: Int? = null,

    var normalCnt: Int? = null,

    var ticketCnt: Int? = null,

    var unrecognizedCnt: Int? = null,

    var parkFee: Int? = null,

    var discountFee: Int? = null,

    var dayDiscountFee: Int? = null,

    var payFee: Int? = null,

    var unPayment: Int? = null,

    var payment: Int? = null
)
