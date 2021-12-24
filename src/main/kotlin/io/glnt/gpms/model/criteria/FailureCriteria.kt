package io.glnt.gpms.model.criteria

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.FacilityCategoryType
import java.io.Serializable
import java.time.LocalDate

data class FailureCriteria (
    var sn: Long? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,

    var resolved: String? = null,

    var category: String? = null

): Serializable {

    constructor(other: FailureCriteria) :
            this(
                other.sn, other.fromDate, other.toDate, other.resolved, other.category
            )

    companion object {
        private const val serialVersionUID: Long = 1L
    }

}