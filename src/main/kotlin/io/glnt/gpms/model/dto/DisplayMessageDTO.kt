package io.glnt.gpms.model.dto

import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class DisplayMessageDTO(
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var messageClass: DisplayMessageClass? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var messageType: DisplayMessageType? = null,

    var messageCode: String? = null,

    var order: Int? = null,

    var lineNumber: Int? = null,

    var colorCode: String? = null,

    @get: NotNull
    var messageDesc: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var displayColor: DisplayColorDTO? = null
): Serializable {
    constructor(displayMessage: DisplayMessage) :
            this(
                displayMessage.sn, displayMessage.messageClass, displayMessage.messageType, displayMessage.messageCode,
                displayMessage.order, displayMessage.lineNumber, displayMessage.colorCode, displayMessage.messageDesc, displayMessage.delYn
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayMessageDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31

    override fun toString() = "DisplayMessage{" +
            "sn=$sn" +
            ", messageClass='$messageClass'" +
            ", messageType='$messageType'" +
            ", order='$order'" +
            ", lineNumber='$lineNumber'" +
            ", colorCode='$colorCode'" +
            ", messageDesc='$messageDesc'" +
            ", delYn='$delYn'" +
            "}"
}
