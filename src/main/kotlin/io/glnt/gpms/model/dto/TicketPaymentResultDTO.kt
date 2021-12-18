package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.ResultType
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class TicketPaymentResultDTO(
    var inPaymentSn: String? = null,

    @get: NotNull
    var sn: String? = null,

    @get: NotNull
    var effectDate: String? = null,

    @get: NotNull
    var expireDate: String? = null,

    @get: NotNull
    var price: String? = null,

    var name: String? = null,

    var vehicleNumber: String? = null,

    var paymentMachineType: String? = null,

    var paymentType: String? = null,

    var approveDatetime: String? = null,

    var cardAmount: Int? = null,

    var cardCorp: String? = null,

    var cardNumber: String? = null,

    var paymentAmount: String? = null,

    var transactionId: String? = null,

    var failureMessage: String? = null,
    @Enumerated(EnumType.STRING)
    var result: ResultType? = null
)
