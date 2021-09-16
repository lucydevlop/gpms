package io.glnt.gpms.model.dto

import javax.validation.constraints.NotNull

data class StatisticsInoutCountDTO(
    @get: NotNull
    var date: String? = null,

    var inCnt: Int? = null,

    var outCnt: Int? = null,

    var inNormalCnt: Int? = null,

    var outNormalCnt: Int? = null,

    var inTicketCnt: Int? = null,

    var outTicketCnt: Int? = null,

    var inUnrecognizedCnt: Int? = null,

    var outUnrecognizedCnt: Int? = null
)
