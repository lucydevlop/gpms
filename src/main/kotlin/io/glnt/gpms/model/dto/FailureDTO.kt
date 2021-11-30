package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.Failure
import io.glnt.gpms.model.enums.Yn
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
    var syncYn: Yn? = null
): Serializable {
    constructor(failure: Failure):
            this(
                failure.sn, failure.issueDateTime, failure.expireDateTime, failure.facilitiesId,
                failure.fName, failure.failureCode, failure.failureType, failure.failureFlag, failure.syncYn
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FailureDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}
