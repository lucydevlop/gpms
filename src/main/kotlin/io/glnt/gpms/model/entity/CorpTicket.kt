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
@Table(schema = "glnt_parking", name="tb_corpticket")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CorpTicket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "flag", nullable = false)
    var flag: Int = 0,

    @Column(name = "corpId", nullable = false)
    var corpId: String,

    @Column(name = "corp_class_sn", insertable = true, updatable = true)
    var corpClassSn: Long,

    @Column(name = "corpSn", nullable = false)
    var corpSn: Long,

    @Column(name = "disUse", nullable = false)
    var disUse: String,

    @Column(name = "unitTime")
    var unitTime: Int?,

    @Column(name = "disMaxNo")
    var disMaxNo: Int?,

    @Column(name = "disMaxDay")
    var disMaxDay: Int?,

    @Column(name = "disMaxMonth")
    var disMaxMonth: Int?,

    @Column(name = "disPrice")
    var disPrice: Int?,

    @Column(name = "quantity", nullable = false)
    var quantity: Int? = 1,

    @Column(name = "effectDate", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @Column(name = "expireDate", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59"),

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = true)
    var delYn: DelYn? = DelYn.N
) : Auditable(), Serializable {

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "corp_class_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var discountClass: DiscountClass? = null

    var ableCnt: Int? = 0
}
