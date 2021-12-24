package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.*
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_corp_ticket_class")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class CorpTicketClass (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true)
    var sn: Long? = null,

    @Column(unique = true)
    var name: String? = null,

    @Column(name = "effect_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.stringToLocalDateTime(DateUtil.nowDateTime, "yyyy-MM-dd HH:mm:ss"),

    @Column(name = "expire_date")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),

    @Column(name = "discount_class_sn")
    var discountClassSn: Long? = null,

    @Column(name = "once_max")
    var onceMax: Long? = 1,

    @Column(name = "day_max")
    var dayMax: Long? = 1,

    @Column(name = "month_max")
    var monthMax: Long? = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = true)
    var saleType: SaleType? = SaleType.FREE,

    var price: Long? = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "extend_yn")
    var extendYn: OnOff? = OnOff.OFF,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn")
    var delYn: YN? = null,

    @Enumerated(EnumType.STRING)
    var applyTarget: DiscountApplyTargetType? = null,

    @Enumerated(EnumType.STRING)
    var applyType: DiscountRangeType? = null,

    ): Auditable(), Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CorpTicketClass) return false
        if (other.sn == null || sn == null) return false

        return Objects.equals(sn, other.sn)
    }

    override fun hashCode(): Int {
        return 31
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}