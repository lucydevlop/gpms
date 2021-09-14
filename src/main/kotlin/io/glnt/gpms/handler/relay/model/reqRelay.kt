package io.glnt.gpms.handler.relay.model

data class reqRelayHealthCheck(
    var facilitiesList: ArrayList<FacilitiesHealthCheck>
)

data class FacilitiesHealthCheck(
    var dtFacilitiesId: String,
    var failureAlarm: String? = null,
    var status: String? = null,
    var responseId: String? = null
)

data class FacilitiesStatusNoti(
    var dtFacilitiesId: String? = null,
    var STATUS: String,
    var facilitiesId: String? = null
)

data class FacilitiesFailureAlarm(
    var dtFacilitiesId: String? = null,
    var failureAlarm: String,
    var facilitiesId: String? = null
)

data class paystationvehicleListSearch(
    var vehicleNumber: String,
    var inVehicleDateTime: String,
    var inSn: String
)