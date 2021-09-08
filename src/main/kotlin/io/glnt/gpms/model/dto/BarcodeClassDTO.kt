package io.glnt.gpms.model.dto

import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.entity.Barcode
import io.glnt.gpms.model.entity.BarcodeClass
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class BarcodeClassDTO (
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var start: Int? = null,

    var end: Int? = null,

    var discountClass: DiscountClassDTO? = null,

    var discountClassSn: Long,

): Serializable {

    constructor(barcodeClass: BarcodeClass) :
        this(
            barcodeClass.sn, barcodeClass.delYn, barcodeClass.start, barcodeClass.end,
            discountClass = null,
            barcodeClass.discountClassSn
        )


}