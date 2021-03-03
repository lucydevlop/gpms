package io.glnt.gpms.handler.dashboard.admin.model

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