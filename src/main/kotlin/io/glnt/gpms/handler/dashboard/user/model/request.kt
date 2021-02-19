package io.glnt.gpms.handler.dashboard.user.model

import java.time.LocalDateTime

data class reqVehicleSearch(
    var vehicleNo: String,
    var inDate: LocalDateTime? = null
)
