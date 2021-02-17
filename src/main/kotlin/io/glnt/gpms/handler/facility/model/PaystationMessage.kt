package io.glnt.gpms.handler.facility.model

data class reqParkingSiteInfo(
    var parkingSiteName: String,
    var lotNumberAddress: String,
    var roadNameAddress: String,
    var detailsAddress: String,
    var telephoneNumber: String,
    var saupno: String,
    var businessName: String
)

data class reqPayStationData(
    var paymentMachineType: String,
    var vehicleNumber: String,
    var facilitiesId: String? = null,
    var recognitionType: String? = null,
    var recognitionResult: String? = null,
    var paymentAmount: String,
    var parktime: String? = null,
    var vehicleIntime: String? = null,
    var adjustmentDateTime: String? = null,
    var paymentType: String? = null,
    var transactionId: String? = null,
    var cardAmount: String? = null,
    var parkTicketAmount: String? = null,
    var cardNumber: String? = null,
    var approveDatetime: String? = null,
    var cardCorp: String? = null,
    var chargingId: String? = null
)

data class reqPayData(
    var paymentMachineType: String,
    var vehicleNumber: String,
    var parkTicketType: String,
    var parkTicketMoney: String,
    var facilitiesId: String? = null
)

data class reqPaystation(
    var facilityId: String,
    var data: Any
)

data class reqPaymentResponse(
    var chargingId: String? = null,
    var vehicleNumber: String
)

data class reqPaymentResult(
    var vehicleNumber: String,
    var paymentMachineType: String,
    var paymentType: String,
    var approveDatetime: String? = null,
    var cardAmount: Int? = null,
    var cardCorp: String? = null,
    var cardNumber: String? = null,
    var parkTicketAmount: String? = null,
    var paymentAmount: String? = null,
    var transactionId: String? = null,
    var failureMessage: String? = null
)


