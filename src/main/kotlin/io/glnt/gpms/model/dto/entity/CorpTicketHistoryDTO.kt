package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.CorpTicketHistory
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class CorpTicketHistoryDTO(
    var sn: Long? = null,

    var ticketSn: Long? = null,

    var totalQuantity: Int? = 0,

    var useQuantity: Int? = 0,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    var corpTicket: CorpTicketDTO? = null
) : Serializable {

    constructor(corpTicketHistory: CorpTicketHistory) :
        this(
            corpTicketHistory.sn, corpTicketHistory.ticketSn, corpTicketHistory.totalQuantity,
            corpTicketHistory.useQuantity, corpTicketHistory.effectDate, corpTicketHistory.expireDate,
            corpTicketHistory.delYn
        )
}