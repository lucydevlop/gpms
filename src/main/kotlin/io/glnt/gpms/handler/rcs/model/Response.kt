package io.glnt.gpms.handler.rcs.model

import io.glnt.gpms.model.enums.*
import java.time.LocalDateTime

data class ResAsyncFacility(
    var sn: Long,
    var category: FacilityCategoryType,
    var modelid: String,
    var fname: String,
    var dtFacilitiesId: String,
    var facilitiesId: String? = null,
    var gateId: String,
    var gateName: String,
    var ip: String,
    var port: String,
    var lprType: LprTypeStatus? = null,
    var imagePath: String? = null,
    var health: String? = null,
    var healthDate: LocalDateTime? = null,
    var status: String? = null,
    var statusDate: LocalDateTime? = null,
    var gateType: GateTypeStatus,
    val delYn: YN,
    val resetPort: Int?
)


data class ResAsyncParkinglot(
    var msg: String,
    var code: Int,
    var data: Any
)