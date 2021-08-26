package io.glnt.gpms.model.dto

import io.glnt.gpms.model.dto.CorpDTO

data class CorpTicketSummaryDTO(
    var corp: CorpDTO,
    var tickets: ArrayList<HashMap<String, Any?>>
)
