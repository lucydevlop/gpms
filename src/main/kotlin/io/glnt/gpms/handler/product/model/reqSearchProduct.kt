package io.glnt.gpms.handler.product.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.TicketType
import java.time.LocalDate
import java.time.LocalDateTime

data class reqSearchProduct(
    @JsonFormat(pattern = "yyyy-MM-dd") var from: LocalDate,
    @JsonFormat(pattern = "yyyy-MM-dd") var to: LocalDate
)

data class reqCreateProduct(
    var vehicleNo: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var startDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var endDate: LocalDateTime,
    var userId: String? = null,
    var gateId: MutableSet<String>? = mutableSetOf("ALL"),
    var ticktType: TicketType? = TicketType.SEASONTICKET
)
