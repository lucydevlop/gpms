package io.glnt.gpms.handler.relay.model

data class reqRelayHealthCheck(
    var facilitiesList: ArrayList<FacilitiesHealthCheck>
)

data class FacilitiesHealthCheck(
    var facilitiesId: String,
    var failureAlarm: String? = null,
    var healthStatus: String? = null
)

data class FacilitiesStatusNoti(
    var facilitiesId: String,
    var STATUS: String
)

data class FacilitiesFailureAlarm(
    var facilitiesId: String,
    var failureAlarm: String
)

data class paystationvehicleListSearch(
    var vehicleNumber: String,
    var inVehicleDateTime: String
)