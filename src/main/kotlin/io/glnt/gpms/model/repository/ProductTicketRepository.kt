package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ProductTicket
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ProductTicketRepository: JpaRepository<ProductTicket, Long> {
    fun findByVehicleNoAndValidDateGreaterThanEqualAndRegDateLessThanEqualAndFlagIsNullOrFlag(vehiclNo: String, date1: LocalDateTime, date2: LocalDateTime, flag: Int): ProductTicket?
    fun findByVehicleNoAndValidDateGreaterThanEqualAndFlagIsNullOrFlag(vehiclNo: String, date1: LocalDateTime, flag: Int): ProductTicket?
    fun findAll(specification: Specification<ProductTicket>): List<ProductTicket>?

}