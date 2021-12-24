package io.glnt.gpms.model.criteria

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.TicketType
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime

data class SeasonTicketCriteria (
    var sn: Long? = null,

    var searchLabel: String? = null,

    var searchText: String? = null,

    var ticketType: TicketType? = null,

    var searchDateLabel: DateType? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss") var effectDate: LocalDateTime? = null,

    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss") var expireDate: LocalDateTime? = null,

    var delYn: String? = null

): Serializable {
    constructor(other: SeasonTicketCriteria) :
            this(
                other.sn,
                other.searchLabel,
                other.searchText,
                other.ticketType,
                other.searchDateLabel,
                other.fromDate,
                other.toDate,
                other.effectDate,
                other.expireDate,
                other.delYn
            )

    companion object {
        private const val serialVersionUID: Long = 1L
    }
}