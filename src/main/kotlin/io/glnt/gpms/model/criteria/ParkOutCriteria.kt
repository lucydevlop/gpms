package io.glnt.gpms.model.criteria

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import java.time.LocalDate

data class ParkOutCriteria (
    var sn: Long? = null,

    var uuid: String? = null,

    var vehicleNo: String? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,

    var parkcartype: String? = null,

    var gateId: String? = null,

    var delYn: DelYn? = null

): Serializable {
    constructor(other: ParkOutCriteria) :
        this(
            other.sn,
            other.uuid,
            other.vehicleNo,
            other.fromDate,
            other.toDate,
            other.parkcartype,
            other.gateId,
            other.delYn
        )

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}