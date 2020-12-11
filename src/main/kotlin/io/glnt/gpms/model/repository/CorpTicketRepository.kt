package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CorpTicket
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CorpTicketRepository: JpaRepository<CorpTicket, Long> {
    fun findByCorpId(corpId: String) : List<CorpTicket>?
}