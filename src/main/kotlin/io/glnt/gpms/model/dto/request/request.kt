package io.glnt.gpms.model.dto.request

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.enums.VehicleType
import java.time.LocalDate
import java.time.LocalDateTime

data class reqCreateProductTicket(
    var sn: Long? = null,
    var vehicleNo: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime,
    var userId: String? = null,
    var gateId: MutableSet<String>? = null,
    var ticketType: TicketType? = null,
    var vehicleType: VehicleType? = null,
    var corpSn: Long? = null,
    var corpName: String? = null,
    var etc: String? = null,
    var name: String? = null,
    var etc1: String? = null,
    var tel: String? = null,
    var vehiclekind: String? = null
)

data class reqSearchProductTicket(
    var searchLabel: String? = null,
    var searchText: String? = null,
    var searchDateLabel: DateType? = DateType.EFFECT,
    @JsonFormat(pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime? = null,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime? = null,
    var ticketType: TicketType? = null,
    var delYn: String? = "N"
)