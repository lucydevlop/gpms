package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CorpTicketClass
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CorpTicketClassRepository : JpaRepository<CorpTicketClass, Long>{
}