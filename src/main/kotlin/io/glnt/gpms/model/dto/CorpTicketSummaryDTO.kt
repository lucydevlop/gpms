package io.glnt.gpms.model.dto

import io.glnt.gpms.model.dto.entity.CorpDTO

data class CorpTicketSummaryDTO(
    var corp: CorpDTO,
    var tickets: ArrayList<HashMap<String, Any?>>
)
