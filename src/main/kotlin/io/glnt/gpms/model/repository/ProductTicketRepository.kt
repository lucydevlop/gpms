package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface ProductTicketRepository: JpaRepository<ProductTicket, Long> {
    fun findByVehicleNoAndValidDateGreaterThanEqualAndRegDateLessThanEqualAndDelYn(vehiclNo: String, date1: LocalDateTime, date2: LocalDateTime, delYn: DelYn): ProductTicket?
    fun findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(vehiclNo: String, date1: LocalDateTime, date2: LocalDateTime, delYn: DelYn): ProductTicket?
    fun findByVehicleNoAndValidDateGreaterThanEqualAndDelYn(vehiclNo: String, date1: LocalDateTime, delYn: DelYn): ProductTicket?
    fun findAll(specification: Specification<ProductTicket>): List<ProductTicket>?
    fun findBySn(sn: Long): ProductTicket?
    fun findByVehicleNoAndEffectDateAndExpireDateAndTicketTypeAndDelYn(vehiclNo: String, effectDate: LocalDateTime, expireDate: LocalDateTime, ticketType: TicketType, delYn: DelYn): List<ProductTicket>?

}