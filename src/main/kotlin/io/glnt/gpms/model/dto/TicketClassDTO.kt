package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.TicketClass
import io.glnt.gpms.model.enums.*
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class TicketClassDTO(
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var ticketType: TicketType? = TicketType.SEASONTICKET,

    @get: NotNull
    var ticketName: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var aplyType: TicketAplyType? = TicketAplyType.FULL,

    var startTime: String? = "0000",
    var endTime: String? = "2400",

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var rangeType: DiscountRangeType? = DiscountRangeType.ALL,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59"),

    var price: Int? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var vehicleType: VehicleType? = VehicleType.SMALL,

    var available: Int? = null, //구매 이후 1년 사용 가능(시간권일 경우)

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null
): Serializable {
    constructor(ticketClass: TicketClass) :
        this(
            ticketClass.sn, ticketClass.ticketType, ticketClass.ticketName, ticketClass.aplyType, ticketClass.startTime, ticketClass.endTime,
            ticketClass.rangeType, ticketClass.effectDate, ticketClass.expireDate, ticketClass.price, ticketClass.vehicleType, ticketClass.available, ticketClass.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TicketClassDTO) return false
        return sn != null && sn == other.sn
    }

    override fun toString() = "TicketClass{" +
        "sn=$sn" +
        ", ticketType='$ticketType'" +
        ", ticketName='$ticketName'" +
        ", aplyType='$aplyType'" +
        ", rangeType='$rangeType'" +
        "}"
}
