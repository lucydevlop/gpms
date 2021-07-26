package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.BarcodeTickets
import org.springframework.data.jpa.repository.JpaRepository

interface BarcodeTicketsRepository: JpaRepository<BarcodeTickets, Long> {
}