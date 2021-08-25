package io.glnt.gpms.model.dto

import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class CorpTicketDTO(
    var sn: Long? = null,

    var corpSn: Long? = null,

    var discountClassSn: Long? = null,

    var totalQuantity: Int? = 0,

    var useQuantity: Int? = 0,

    var orderNum: Int? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var discountClass: DiscountClassDTO? = null,

    var corp: CorpDTO? = null,

    var ableCnt: Int? = 0

) : Serializable {

    constructor(corpTicket: CorpTicketInfo) :
        this(
            corpTicket.sn, corpTicket.corpSn, corpTicket.discountClassSn,
            corpTicket.totalQuantity, corpTicket.useQuantity, corpTicket.orderNum, corpTicket.delYn
        )
}