package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class CorpTicketDTO(
    var sn: Long? = null,

    var corpSn: Long? = null,

    var classSn: Long? = null,

    var totalQuantity: Int? = 0,

    var useQuantity: Int? = 0,

    var orderNum: Int? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    var corpTicketClass: CorpTicketClassDTO? = null,

    var corp: CorpDTO? = null,

    var ableCnt: Int? = 0,

    var todayUse: Int? = 0,

    var totalCnt: Int? = 0,


    ) : Serializable {

    constructor(corpTicket: CorpTicketInfo) :
        this(
            corpTicket.sn, corpTicket.corpSn, corpTicket.classSn,
            corpTicket.totalQuantity, corpTicket.useQuantity, corpTicket.orderNum, corpTicket.delYn
        )
}