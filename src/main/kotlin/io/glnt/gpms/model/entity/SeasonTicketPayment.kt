package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.PayType
import io.glnt.gpms.model.enums.ResultType
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_seasonTicket_payment")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SeasonTicketPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false, insertable = true, updatable = false)
    var sn: Long?,

    @Column(name = "ticket_sn")
    var ticketSn: Long? = null,

    @Column(name = "vehicle_no")
    var vehicleNo: String? = null,

    @Column(name = "approve_datetime")
    var approveDateTime: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type")
    var payType: PayType? = PayType.CARD,

    @Column(name = "amount")
    var amount: Int? = 0,

    @Column(name = "card_corp")
    var cardCorp: String? = null,

    @Column(name = "card_number")
    var cardNumber: String? = null,

    @Column(name = "transaction_id")
    var transactionId: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "result")
    var result: ResultType? = null,

    @Column(name = "failure_message")
    var failureMessage: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

) : Auditable(), Serializable {

}