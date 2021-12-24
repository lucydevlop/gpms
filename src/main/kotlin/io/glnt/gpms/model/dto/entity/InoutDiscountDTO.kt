package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.TicketType
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class InoutDiscountDTO (
    var sn: Long? = null,

    @Enumerated(EnumType.STRING)
    var discountType: TicketType? = TicketType.CORPTICKET,

    var corpSn: Long? = null,

    var discountClassSn: Long? = null,

    var ticketHistSn: Long? = null,

    var ticketClassSn: Long? = null,

    var inSn: Long? = null,

    var quantity: Int? = 1,

    var useQuantity: Int? = null,

    var applyDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var calcYn: YN? = YN.N,

    var outSn: Long? = null,

    var createDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var delYn: YN? = YN.N,

    var discountClass: DiscountClassDTO? = null,

    var parkInDTO: ParkInDTO? = null
): Serializable {
    constructor(inoutDiscount: InoutDiscount) :
        this(
            inoutDiscount.sn, inoutDiscount.discontType, inoutDiscount.corpSn, inoutDiscount.discountClassSn,
            inoutDiscount.ticketHistSn, inoutDiscount.ticketClassSn, inoutDiscount.inSn, inoutDiscount.quantity, inoutDiscount.useQuantity,
            inoutDiscount.applyDate, inoutDiscount.calcYn, inoutDiscount.outSn, inoutDiscount.createDate, inoutDiscount.delYn
        )
}