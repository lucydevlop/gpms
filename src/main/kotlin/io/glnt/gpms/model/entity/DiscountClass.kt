package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.*
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_discount_class")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscountClass(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    var discountType: DiscountType? = DiscountType.DISCOUNT,

    @Column(name = "discount_name", nullable = false)
    var discountNm: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_apply_type", nullable = true)
    var discountApplyType: DiscountApplyType? = DiscountApplyType.TIME,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_apply_rate")
    var discountApplyRate: DiscountApplyRateType? = DiscountApplyRateType.VARIABLE,

    @Enumerated(EnumType.STRING)
    @Column(name = "time_target", nullable = true)
    var timeTarget: TimeTarget? = TimeTarget.NOW,

    @Enumerated(EnumType.STRING)
    @Column(name = "day_range", nullable = true)
    var dayRange: DiscountRangeType? = DiscountRangeType.ALL,

    @Column(name = "time_range", nullable = true)
    var timeRange: String? = null,

    @Column(name = "unitTime")
    var unitTime: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "disUse", nullable = false)
    var disUse: SaleType? = SaleType.FREE,

    @Column(name = "disMaxNo")
    var disMaxNo: Int? = 1,

    @Column(name = "disMaxDay")
    var disMaxDay: Int? = 9999,

    @Column(name = "disMaxMonth")
    var disMaxMonth: Int? = 9999,

    @Column(name = "disPrice")
    var disPrice: Int? = 0,

    @Column(name = "effectDate")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),

    @Column(name = "expireDate")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59"),

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = true)
    var delYn: DelYn? = DelYn.N
): Auditable(), Serializable {

}

data class timeRange(
    var startTime: String? = "0000",
    var endTime: String? = "2400"
)