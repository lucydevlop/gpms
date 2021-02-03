package io.glnt.gpms.handler.product.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.enums.VehicleType
import java.time.LocalDate
import java.time.LocalDateTime

data class reqSearchProduct(
    var searchLabel: String? = null,
    var searchText: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") var from: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") var to: LocalDate? = null,
    var ticketType: TicketType? = null,
    var delYn: String? = "N"
)

data class reqCreateProduct(
    var sn: Long? = null,
    var vehicleNo: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime,
    var userId: String? = null,
    var gateId: MutableSet<String>? = mutableSetOf("ALL"),
    var ticktType: TicketType? = TicketType.SEASONTICKET,
    var vehicleType: VehicleType? = VehicleType.SMALL,
    var corpSn: Long? = null
)
