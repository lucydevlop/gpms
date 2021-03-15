package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.enums.DelYn
import org.hibernate.annotations.Where
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_corp_ticket_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CorpTicketInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "corp_sn", nullable = false)
    var corpSn: Long,

    @Column(name = "discount_class_sn", nullable = false)
    var discountClassSn: Long,

    @Column(name = "total_quantity", insertable = true, updatable = true)
    var totalQuantity: Int = 0,

    @Column(name = "use_quantity", insertable = true, updatable = true)
    var useQuantity: Int = 0,

    @Column(name = "order_num")
    var orderNum: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = true)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {
    @OneToOne
    @JoinColumn(name = "discount_class_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var discountClass: DiscountClass? = null

    @OneToOne
    @JoinColumn(name = "corp_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var corp: Corp? = null
}

@Entity
@Table(schema = "glnt_parking", name="tb_corp_ticket_history")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CorpTicketHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "ticket_sn")
    var ticketSn: Long,

    @Column(name = "total_quantity", insertable = true, updatable = true)
    var totalQuantity: Int = 0,

    @Column(name = "use_quantity", insertable = true, updatable = true)
    var useQuantity: Int = 0,

    @Column(name = "effectDate", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @Column(name = "expireDate", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = true)
    var delYn: DelYn? = DelYn.N
) : Auditable(), Serializable {

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var ticketInfo: CorpTicketInfo? = null

    var ableCnt = totalQuantity - useQuantity
}
