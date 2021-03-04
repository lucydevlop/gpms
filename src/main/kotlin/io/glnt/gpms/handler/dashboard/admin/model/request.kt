package io.glnt.gpms.handler.dashboard.admin.model

import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.LprTypeStatus

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