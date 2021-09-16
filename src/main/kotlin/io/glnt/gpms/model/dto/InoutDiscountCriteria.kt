package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDate

data class InoutDiscountCriteria (
    var sn: Long? = null,

    var corpSn: Long? = null,

    var ticketClassSn: Long? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,

    ) : Serializable {
    constructor(other: InoutDiscountCriteria):
        this(
            other.sn,
            other.corpSn,
            other.ticketClassSn,
            other.fromDate,
            other.toDate
        )

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}