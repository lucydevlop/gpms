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
    var facilitiesId: String,
    var recognitionType: String,
    var recognitionResult: String,
    var paymentAmount: String,
    var parktime: String? = null,
    var vehicleIntime: String,
    var adjustmentDateTime: String? = null,
    var paymentType: String? = null,
    var transactionId: String? = null,
    var cardAmount: String? = null,
    var parkTicketAmount: String? = null,
    var cardNumber: String? = null,
    var approveDatetime: String? = null,
    var cardCorp: String? = null
)

data class reqPayData(
    var paymentMachineType: String,
    var vehicleNumber: String,
    var parkTicketType: String,
    var parkTicketMoney: String
)

data class reqPaystation(
    var facilityId: String,
    var data: Any
)

