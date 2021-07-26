package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class  BarcodeTicketsDTO (
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    @get: NotNull
    var barcode: String? = null,

    var inSn: Long? = null,

    var vehicleNo: String? = null,

    var price: Long? = null,

    var applyDate: LocalDateTime? = null,

) : Serializable {

}