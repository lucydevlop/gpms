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

data class reqPayStation(
    var paymentMachineType: String,
    var vehicleNumber: String,
    var facilitiesId: String,
    var recognitionType: String,
    var recognitionResult: String,
    var paymentAmount: String,
    var parktime: String? = null,
    var vehicleIntime: String
)

