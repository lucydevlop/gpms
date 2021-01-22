package io.glnt.gpms.handler.relay.model

data class reqRelayHealthCheck(
    var facilitiesList: ArrayList<FacilitiesHealthCheck>
)

data class FacilitiesHealthCheck(
    var facilitiesId: String,
    var failureAlarm: String? = "normal",
    var healthStatus: String? = "NORMAL"
)