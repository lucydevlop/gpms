package io.glnt.gpms.model.dto

import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.entity.ParkOut
import java.io.Serializable

data class StatisticsInoutDTO (
    var inSn: Long? = null,

    var vehicleNo: String? = null,

    var outSn: Long? = null,

    var parkFee: Int? = 0,

    var discountFee : Int? = 0,

    var dayDiscountFee: Int? = 0,

    var payFee: Int? = 0,

    var payment: Int? = 0,

    var nonPayment: Int? = 0
) : Serializable {

}