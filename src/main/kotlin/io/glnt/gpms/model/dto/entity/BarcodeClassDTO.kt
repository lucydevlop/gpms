package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.BarcodeClass
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class BarcodeClassDTO (
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

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