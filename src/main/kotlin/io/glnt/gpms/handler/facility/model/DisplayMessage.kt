package io.glnt.gpms.handler.facility.model

import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqSendDisplay(
    var facilityId: String,
    var messages: ArrayList<reqDisplayMessage>
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
    @Enumerated(EnumType.STRING) var position: DisplayPosition? = null,
    @Enumerated(EnumType.STRING) var type: DisplayType? = null,
    var colorCode: String,
    var colorDesc: String
)

data class reqSetDisplayMessage(
    @Enumerated(EnumType.STRING) var messageClass: DisplayMessageClass? = null,
    @Enumerated(EnumType.STRING) var messageType: DisplayMessageType? = null,
    @Enumerated(EnumType.STRING) var colorType: DisplayType,
    var messageDesc: String,
    var order: Int,
    var line: Int
)