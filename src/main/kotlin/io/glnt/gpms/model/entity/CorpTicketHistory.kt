package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import org.hibernate.annotations.Where
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

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
    var ticketSn: Long? = null,

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
    var delYn: YN? = YN.N
) : Auditable(), Serializable {

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var ticketInfo: CorpTicketInfo? = null

    var ableCnt = totalQuantity - useQuantity

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CorpTicketHistory) return false
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
