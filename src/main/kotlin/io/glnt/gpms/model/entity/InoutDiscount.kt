package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import org.hibernate.annotations.Where
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_inout_discount")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InoutDiscount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false)
    var discontType: TicketType? = TicketType.CORPTICKET,

    @Column(name = "discount_class_sn")
    var discountClassSn: Long? = null,

    @Column(name = "ticket_hist_sn", nullable = true)
    var ticketHistSn: Long? = null,

    @Column(name = "in_sn", nullable = false)
    var inSn: Long,

    @Column(name = "quantity", nullable = false)
    var quantity: Int? = 1,

    @Column(name = "use_quantity")
    var useQuantity: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N,

    @Column(name = "apply_date")
    var applyDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "calc_yn")
    var calcYn: DelYn? = DelYn.N,

    @Column(name = "out_sn")
    var outSn: Long? = null

): Auditable(), Serializable {

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "ticket_hist_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var ticketHist: CorpTicketHistory? = null

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "corp_class_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var discountClass: DiscountClass? = null
}
