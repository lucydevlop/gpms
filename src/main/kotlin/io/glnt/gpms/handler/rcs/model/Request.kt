package io.glnt.gpms.handler.rcs.model

import io.glnt.gpms.model.enums.checkUseStatus
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class ReqFailureAlarm(
    var parkinglotId: Long,
    var facilityId: String,
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    var createDate: String,
    var contents: String,
    @Enumerated(EnumType.STRING)
    var resolvedYn: checkUseStatus? = null
)

data class ReqHealthCheck(
    var dtFacilitiesId: String,
    var health: String? = "ERROR",
    var healthDateTime: String? = null
)

data class ReqFacilityStatus(
    var dtFacilitiesId: String,
    var status: String? = null,
    var statusDateTime: String? = null
)