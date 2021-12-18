package io.glnt.gpms.model.dto

import javax.validation.constraints.NotNull

data class TicketInfoDTO(
    @get: NotNull
    var sn: String? = null,

    @get: NotNull
    var effectDate: String? = null,

    @get: NotNull
    var expireDate: String? = null,

    @get: NotNull
    var price: String? = null,

    var name: String? = null
)
