package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.BarcodeTickets
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class  BarcodeTicketsDTO (
    var sn: Long? = null,

    @get: NotNull
    var barcode: String? = null,

    var inSn: Long? = null,

    var vehicleNo: String? = null,

    var price: Int? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var applyDate: LocalDateTime? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    ) : Serializable {
    constructor(barcodeTickets: BarcodeTickets) :
        this(
            barcodeTickets.sn, barcodeTickets.barcode, barcodeTickets.inSn,
            barcodeTickets.vehicleNo, barcodeTickets.price, barcodeTickets.applyDate, barcodeTickets.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BarcodeTicketsDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31

}