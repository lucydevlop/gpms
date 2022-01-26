package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.CorpTicketClass
import io.glnt.gpms.model.enums.*
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class CorpTicketClassDTO (
    var sn: Long? = null,

    @get: NotNull
    var name: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59"),

    @get: NotNull
    var discountClassSn: Long? = null,

    var onceMax: Long? = null,

    var dayMax: Long? = null,

    var monthMax: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var saleType: SaleType? = null,

    var price: Long? = 0,

    var extendYn: OnOff? = OnOff.OFF,

    var week: MutableSet<String>? = mutableSetOf(WeekType.ALL.toString()),

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    @Enumerated(EnumType.STRING)
    var applyTarget: DiscountApplyTargetType? = DiscountApplyTargetType.NOW,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var applyType: DiscountRangeType? = null,

    var discountClass: DiscountClassDTO? = null
): Serializable {
    constructor(corpTicketClass: CorpTicketClass) :
        this(
            corpTicketClass.sn, corpTicketClass.name, corpTicketClass.effectDate, corpTicketClass.expireDate,
            corpTicketClass.discountClassSn, corpTicketClass.onceMax, corpTicketClass.dayMax, corpTicketClass.monthMax,
            corpTicketClass.saleType, corpTicketClass.price, corpTicketClass.extendYn, corpTicketClass.week, corpTicketClass.delYn,
            corpTicketClass.applyTarget, corpTicketClass.applyType
        )
}