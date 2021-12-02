package io.glnt.gpms.model.criteria

import com.fasterxml.jackson.annotation.JsonFormat
import java.io.Serializable
import java.time.LocalDate

data class InoutPaymentCriteria(
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd")
    var fromDate: LocalDate? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd")
    var toDate: LocalDate? = null,

    var vehicleNo: String? = null
): Serializable {
    constructor(other: InoutPaymentCriteria) :
        this(
            other.fromDate,
            other.toDate,
            other.vehicleNo
        )

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}
