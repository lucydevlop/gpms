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

    @Column(name = "class_sn", nullable = false)
    var classSn: Long,

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
    @JoinColumn(name = "class_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var corpTicketClass: CorpTicketClass? = null

    @OneToOne
    @JoinColumn(name = "corp_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var corp: Corp? = null

    var ableCnt: Int? = 0
}
