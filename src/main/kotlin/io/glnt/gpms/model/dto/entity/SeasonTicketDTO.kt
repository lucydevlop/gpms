package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.SeasonTicket
import io.glnt.gpms.model.enums.*
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class SeasonTicketDTO (
    var sn: Long? = null,

    var corpSn: Long? = null,

    var ticketSn: Long? = null,

    var corpName: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var ticketType: TicketType? = null,

    @get: NotNull
    var vehicleNo: String? = null,

    var color: String? = null,

    var vehiclekind: String? = null,

    @Enumerated(EnumType.STRING)
    var vehicleType: VehicleType? = null,

    var name: String? = null,

    var tel: String? = null,

    var etc: String? = null,

    var etc1: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var extendYn: Yn? = null,

    @Enumerated(EnumType.STRING)
    var payMethod: PayType? = null,

    var nextSn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var corp: CorpDTO? = null,

    var ticket: TicketClassDTO? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var lastInDate: LocalDateTime? = null

) : Serializable {

    constructor(seasonTicket: SeasonTicket) :
        this(
            seasonTicket.sn, seasonTicket.corpSn, seasonTicket.ticketSn, seasonTicket.corpName, seasonTicket.ticketType,
            seasonTicket.vehicleNo, seasonTicket.color, seasonTicket.vehiclekind, seasonTicket.vehicleType, seasonTicket.name,
            seasonTicket.tel, seasonTicket.etc, seasonTicket.etc1, seasonTicket.effectDate, seasonTicket.expireDate, seasonTicket.extendYn, seasonTicket.payMethod, seasonTicket.nextSn, seasonTicket.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SeasonTicketDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}