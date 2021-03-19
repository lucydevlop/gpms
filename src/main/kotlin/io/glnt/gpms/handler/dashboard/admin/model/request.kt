package io.glnt.gpms.handler.dashboard.admin.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.*
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqCreateFacility(
    var fname: String,
    var dtFacilitiesId: String,
    var modelid: String,
    var category: String,
    var gateId: String,
    var facilitiesId: String? = null,
    var ip: String? = null,
    var port: String? = null,
    var sortCount: Int? = null,
    var resetPort: Int? = null,
    var lprType: LprTypeStatus? = null,
    var imagePath: String? = null
)

data class reqCreateMessage(
    var messageClass: DisplayMessageClass,
    var messageType: DisplayMessageType,
    var messageCode: String,
    var order: Int,
    var lineNumber: Int,
    var colorCode: String,
    var messageDesc: String
)

data class reqChangeUseGate(
    var gateId: String,
    var delYn: DelYn
)

data class reqChangeUseFacility(
    var dtFacilitiesId: String,
    var delYn: DelYn
)

data class reqSearchProductTicket(
    var searchLabel: String? = null,
    var searchText: String? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") var from: LocalDate? = null,
    @JsonFormat(pattern = "yyyy-MM-dd") var to: LocalDate? = null,
    var ticketType: TicketType? = null,
    var delYn: String? = "N"
)

data class reqCreateProductTicket(
    var sn: Long? = null,
    var vehicleNo: String,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime,
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime,
    var userId: String? = null,
    var gateId: MutableSet<String>? = null,
    var ticketType: TicketType? = null,
    var vehicleType: VehicleType? = null,
    var corpSn: Long? = null
)

data class reqSearchCorp(
    var corpId: String? = null,
    var searchLabel: String? = null,
    var searchText: String? = null,
    @Enumerated(EnumType.STRING) var useStatus: DelYn? = null
)

data class reqCreateCorpTicket(
    var corpSn: Long,
    var discountClassSn: Long,
    var quantity: Int? = 1
)
