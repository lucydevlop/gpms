package io.glnt.gpms.model.dto

import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.PayType
import io.glnt.gpms.model.enums.ResultType
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class InoutPaymentDTO(
    var sn: Long? = null,

    var type: String? = null,

    @get: NotNull
    var inSn: Long? = null,

    var approveDateTime: String? = null,

    @Enumerated(EnumType.STRING)
    var payType: PayType? = null,

    var amount: Int? = null,

    var cardCorp: String? = null,

    var cardNumber: String? = null,

    var transactionId: String? = null,

    @Enumerated(EnumType.STRING)
    var result: ResultType? = null,

    var failureMessage: String? = null,

    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = DelYn.N,

    var outSn: Long? = null,

    var parkInDTO: ParkInDTO? = null
): Serializable {
    constructor(inoutPayment: InoutPayment):
        this(
            sn = inoutPayment.sn,
            type = inoutPayment.type,
            inSn = inoutPayment.inSn,
            approveDateTime = inoutPayment.approveDateTime,
            payType = inoutPayment.payType,
            amount = inoutPayment.amount,
            cardCorp = inoutPayment.cardCorp,
            cardNumber = inoutPayment.cardNumber,
            transactionId = inoutPayment.transactionId,
            result = inoutPayment.result,
            failureMessage = inoutPayment.failureMessage,
            delYn = inoutPayment.delYn,
            outSn = inoutPayment.outSn
        )
}
