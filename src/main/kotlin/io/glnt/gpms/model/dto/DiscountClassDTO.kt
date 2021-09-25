package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.enums.*
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class DiscountClassDTO(
    var sn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var discountType: DiscountType? = DiscountType.DISCOUNT,

    @get: NotNull
    var discountNm: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var discountApplyType: DiscountApplyType? = DiscountApplyType.TIME,

    @Enumerated(EnumType.STRING)
    var discountApplyRate: DiscountApplyRateType? = DiscountApplyRateType.VARIABLE,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var timeTarget: TimeTarget? = TimeTarget.NOW,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var dayRange: DiscountRangeType? = DiscountRangeType.ALL,

    var timeRange: String? = null,

    var unitTime: Int,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var disUse: SaleType? = SaleType.FREE,

    var disMaxNo: Int? = 1,

    var disMaxDay: Int? = 9999,

    var disMaxMonth: Int? = 9999,

    var disPrice: Int? = 0,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59"),

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,
): Serializable {
    constructor(discountClass: DiscountClass) :
        this(
            discountClass.sn, discountClass.discountType, discountClass.discountNm, discountClass.discountApplyType,
            discountClass.discountApplyRate, discountClass.timeTarget, discountClass.dayRange, discountClass.timeRange,
            discountClass.unitTime, discountClass.disUse, discountClass.disMaxNo, discountClass.disMaxDay, discountClass.disMaxMonth,
            discountClass.disPrice, discountClass.effectDate, discountClass.expireDate, discountClass.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DiscountClassDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31

    override fun toString() = "DiscountClass{" +
        "sn=$sn" +
        ", discountNm='$discountNm'" +
        ", discountType='$discountType'" +
        ", discountApplyType='$discountApplyType'" +
        ", timeTarget='$timeTarget'" +
        "}"

}
