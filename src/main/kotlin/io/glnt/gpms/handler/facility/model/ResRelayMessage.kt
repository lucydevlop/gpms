package io.glnt.gpms.handler.facility.model

import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.LprTypeStatus

data class resRelaySvrFacility(
    var sn: Long?,
    var category: String,
    var modelid: String,
    var fname: String,
    var dtFacilitiesId: String,
    var facilitiesId: String?,
    var flagUse: Int?,
    var gateId: String,
    var udpGateid: String?,
    var ip: String?,
    var port: String?,
    var sortCount: Int?,
    var resetPort: Int?,
    var flagConnect: Int?,
    var lprType: LprTypeStatus?,
    var imagePath: String?,
    var gateType: GateTypeStatus,
    var relaySvrKey: String?
)

