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

data class ReqCreateMessage(
    var messageClass: DisplayMessageClass,
    var messageType: DisplayMessageType,
    var messageCode: String,
    var order: Int,
    var lineNumber: Int,
    var colorCode: String,
    var messageDesc: String,
    var sn: Long? = null
)

data class reqChangeUseGate(
    var gateId: String,
    var delYn: DelYn
)

data class reqChangeUseFacility(
    var dtFacilitiesId: String,
    var delYn: DelYn
)

data class reqSearchCorp(
    var corpId: String? = null,
    var searchLabel: String? = null,
    var searchText: String? = null,
    @Enumerated(EnumType.STRING) var useStatus: DelYn? = null
)

data class reqSearchItem(
    var searchLabel: String? = null,
    var searchText: String? = null,
    var searchRoles: List<UserRole>? = null
)

data class reqCreateCorpTicket(
    var corpSn: Long,
    var discountClassSn: Long,
    var quantity: Int? = 1
)
