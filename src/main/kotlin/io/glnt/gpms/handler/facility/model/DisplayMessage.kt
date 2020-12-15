package io.glnt.gpms.handler.facility.model

import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqDisplayMessage(
    var line1: DisplayLine,
    var line2: DisplayLine? = null,
    var line3: DisplayLine? = null,
    var line4: DisplayLine? = null
)

data class DisplayLine(
    var color: String,
    var text: String
)

data class reqSetDisplayColor(
    @Enumerated(EnumType.STRING) var position: DisplayPosition? = null,
    @Enumerated(EnumType.STRING) var type: DisplayType? = null,
    var colorCode: String,
    var colorDesc: String
)