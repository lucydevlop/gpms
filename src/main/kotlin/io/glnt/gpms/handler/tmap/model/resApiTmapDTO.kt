package io.glnt.gpms.handler.tmap.model

data class resApiTmapCommon (
    var type: String? = null,
    var parkingSiteId: String,
    var responseId: String? = null,
    var commandDateTime: String,
    var contents: Any
)

data class resTmapInVehicle(
    var result: String,
    var sessionId: String? = null,
    var errorMsg: String?,
    var memberType: String?,
    var messageType: String?,
    var undecideMessage: String?,
    var openYn: String?,
    var refuseReason: String?
)
