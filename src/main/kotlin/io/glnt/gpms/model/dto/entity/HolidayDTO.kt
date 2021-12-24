package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.Holiday
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.HolidayType
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class HolidayDTO (
    var sn: Long?,

    @get: NotNull
    var name: String? = null,

    @get: NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var startDate: LocalDateTime?,

    @get: NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var endDate: LocalDateTime?,

    var startTime: String? = null,
    var endTime: String? = null,

    @get: NotNull
    var isWorking: Boolean?,

    @get: NotNull
    var delYn: YN? = null,

    var type: HolidayType?
): Serializable {

    constructor(holiday: Holiday) :
        this(
            holiday.sn,
            holiday.name,
            holiday.startDate,
            holiday.endDate,
            holiday.startTime,
            holiday.endTime,
            holiday.isWorking,
            holiday.delYn,
            holiday.type
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is HolidayDTO) return false
        return (name != null && name == other.name) && (startDate == other.startDate) && (endDate == other.endDate)
    }

    override fun hashCode() = 31
}