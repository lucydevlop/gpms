package io.glnt.gpms.handler.facility.model

import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
//import io.glnt.gpms.model.enums.DisplayType
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqSendDisplay(
    var dtFacilityId: String,
    var messages: ArrayList<reqDisplayMessage>,
    var reset: String? = "on",
)

data class reqDisplayMessage(
    var color: String,
    var text: String,
    var order: Int,
    var line: Int
)

//data class DisplayLine(
//    var color: String,
//    var text: String,
//    var order: Int,
//    var line: Int
//)

data class reqSetDisplayColor(
    var colorCode: String,
    var colorDesc: String
)

data class reqSetDisplayMessage(
    @Enumerated(EnumType.STRING) var messageClass: DisplayMessageClass? = null,
    @Enumerated(EnumType.STRING) var messageType: DisplayMessageType? = null,
    var colorCode: String,
    var messageDesc: String,
    var order: Int,
    var line: Int,
    var sn: Long?=null
)

