package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.DisplayColor
import java.io.Serializable
import javax.validation.constraints.NotNull

data class DisplayColorDTO (
    var sn: Long? = null,

    @get: NotNull
    var colorCode: String? = null,

    var colorDesc: String? = null
): Serializable {
    constructor(displayColor: DisplayColor) :
            this(
                displayColor.sn, displayColor.colorCode, displayColor.colorDesc
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DisplayColorDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31

    override fun toString() = "DisplayColor{" +
            "sn=$sn" +
            ", colorCode='$colorCode'" +
            ", colorDesc='$colorDesc'" +
            "}"
}
