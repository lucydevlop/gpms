package io.glnt.gpms.model.dto.rcs

import io.glnt.gpms.model.dto.CorpTicketClassDTO
import io.glnt.gpms.model.dto.TicketClassDTO

data class RcsProductsDTO(
    val ticketClasses: List<TicketClassDTO>? = null,
    val corpTicketClasses: List<CorpTicketClassDTO>? = null
)
