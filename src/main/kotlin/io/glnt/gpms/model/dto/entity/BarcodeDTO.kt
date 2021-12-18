package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.Barcode
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

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = null,

    @get: NotNull
    var startIndex: Int? = null,

    @get: NotNull
    var endIndex: Int? = null,

    var decriptKey: String? = null
): Serializable {

    constructor(barcode: Barcode) :
        this(
            barcode.sn, barcode.delYn, barcode.effectDate,
            barcode.expireDate, barcode.startIndex, barcode.endIndex, barcode.decriptKey
        )

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