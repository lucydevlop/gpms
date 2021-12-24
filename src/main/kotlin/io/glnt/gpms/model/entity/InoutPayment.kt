package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.PayType
import io.glnt.gpms.model.enums.ResultType
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_inout_payment")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InoutPayment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "type")
    var type: String? = null,

    @Column(name = "in_sn")
    var inSn: Long? = null,

    @Column(name = "out_sn")
    var outSn: Long? = null,

    @Column(name = "vehicle_no")
    var vehicleNo: String? = null,

    @Column(name = "approve_datetime")
    var approveDateTime: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type")
    var payType: PayType? = PayType.CARD,

    @Column(name = "park_time")
    var parkTime: Int? = 0,

    @Column(name = "park_fee")
    var parkFee: Int? = 0,

    @Column(name = "discount")
    var discount: Int? = 0,

    @Column(name = "day_discount")
    var dayDiscount: Int? = 0,

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
    var delYn: YN? = YN.N
): Auditable(), Serializable {

}
