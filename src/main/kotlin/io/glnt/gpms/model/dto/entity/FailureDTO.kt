package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class FailureDTO(
    var sn: Long?,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var issueDateTime: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDateTime: LocalDateTime? = null,

    var facilitiesId: String? = null,

    var fName: String? = null,

    var failureCode: String? = null,

    var failureType: String? = null,

    var failureFlag: Int? = 0,

    @Enumerated(EnumType.STRING)
    var syncYn: YN? = null,

    var category: FacilityCategoryType? = null,

    var gateId: String? = null,

    var gateName: String? = null
): Serializable {
    constructor(failure: Failure):
            this(
                failure.sn, failure.issueDateTime, failure.expireDateTime, failure.facilitiesId,
                failure.fName, failure.failureCode, failure.failureType, failure.failureFlag, failure.syncYn, failure.category, failure.gateId
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FailureDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}
