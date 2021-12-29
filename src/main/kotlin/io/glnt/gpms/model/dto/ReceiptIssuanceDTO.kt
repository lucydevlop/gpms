package io.glnt.gpms.model.dto

import java.io.Serializable
import javax.validation.constraints.NotNull

data class ReceiptIssuanceDTO(
    @get: NotNull
    var vehicleNumber: String? = null,

    var inVehicleDateTime: String? = null,

    var parkingTimes: String? = null,

    var parkingAmount: String? = null,

    var discountAmount: String? = null,

    var adjustmentAmount: String? = null,

    var cardNumber: String? = null,

    var cardCorp: String? = null,

    var transactionId: String? = null,

    var adjustmentDateTime: String? = null
): Serializable {

}
