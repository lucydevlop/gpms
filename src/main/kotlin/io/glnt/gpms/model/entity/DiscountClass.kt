package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DiscountRangeType
import io.glnt.gpms.model.enums.DiscountSaleType
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_corpclass")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DiscountClass(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "flag", nullable = false)
    var flag: Int = 0,

    @Column(name = "class", nullable = false)
    var discountNm: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "range1", nullable = false)
    var range1: DiscountRangeType? = DiscountRangeType.ALL,

    @Column(name = "unitTime")
    var unitTime: Int,

    @Enumerated(EnumType.STRING)
    @Column(name = "disUse", nullable = false)
    var disUse: DiscountSaleType? = DiscountSaleType.FREE,

    @Column(name = "disMaxNo")
    var disMaxNo: Int? = 1,

    @Column(name = "disMaxDay")
    var disMaxDay: Int? = 9999,

    @Column(name = "disMaxMonth")
    var disMaxMonth: Int? = 9999,

    @Column(name = "disPrice")
    var disPrice: Int? = 0,

    @Column(name = "effectDate")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),

    @Column(name = "expireDate")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59")
): Auditable(), Serializable {

}
