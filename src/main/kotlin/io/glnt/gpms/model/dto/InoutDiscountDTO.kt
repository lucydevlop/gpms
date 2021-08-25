package io.glnt.gpms.model.dto

import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class InoutDiscountDTO (
    var sn: Long? = null,

    @Enumerated(EnumType.STRING)
    var discontType: TicketType? = TicketType.CORPTICKET,

    var corpSn: Long? = null,

    var discountClassSn: Long? = null,

    var ticketHistSn: Long? = null,

    var inSn: Long? = null,

    var quantity: Int? = 1,

    var useQuantity: Int? = null,

    var applyDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var calcYn: DelYn? = DelYn.N,

    var outSn: Long? = null,

    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = DelYn.N,

    var discountClass: DiscountClassDTO? = null
): Serializable {
    constructor(inoutDiscount: InoutDiscount) :
        this(
            inoutDiscount.sn, inoutDiscount.discontType, inoutDiscount.corpSn, inoutDiscount.discountClassSn,
            inoutDiscount.ticketHistSn, inoutDiscount.inSn, inoutDiscount.quantity, inoutDiscount.useQuantity,
            inoutDiscount.applyDate, inoutDiscount.calcYn, inoutDiscount.outSn, inoutDiscount.delYn
        )
}