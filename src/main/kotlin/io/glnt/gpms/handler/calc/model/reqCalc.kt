package io.glnt.gpms.handler.calc.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.VehicleType
import java.time.LocalDateTime

data class reqCalc(
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") var inTime: LocalDateTime,
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss") var outTime: LocalDateTime?,
    var vehicleType: VehicleType,
    var vehicleNo: String?,
    var type: Int,
    var discountMin: Int,
    val inSn: Long?
)