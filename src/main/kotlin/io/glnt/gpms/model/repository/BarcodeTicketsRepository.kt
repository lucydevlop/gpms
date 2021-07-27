package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.BarcodeTickets
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarcodeTicketsRepository: JpaRepository<BarcodeTickets, Long> {
}