package io.glnt.gpms.handler.dashboard.user.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class resVehicleSearch(
    var sn: Long,
    var vehicleNo: String,
    var inDate: String,
    var imImagePath: String? = null
)
