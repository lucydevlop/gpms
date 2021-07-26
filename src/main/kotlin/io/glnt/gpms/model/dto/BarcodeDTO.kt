package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class BarcodeDTO (
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var effectDate: LocalDateTime? = null,

    var expireDate: LocalDateTime? = null,

    @get: NotNull
    var startIndex: Int? = null,

    @get: NotNull
    var endIndex: Int? = null,

    var decriptKey: String? = null
): Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BarcodeDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31

    override fun toString() = "Barcode{" +
        "sn=$sn" +
        ", effectDate='$effectDate'" +
        ", expireDate='$expireDate'" +
        ", startIndex='$startIndex'" +
        ", endIndex='$endIndex'" +
        "}"
}