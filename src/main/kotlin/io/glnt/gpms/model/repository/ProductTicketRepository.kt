package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.entity.TicketClass
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
    fun findByVehicleNoAndTicketTypeAndDelYnAndEffectDateGreaterThanAndExpireDateLessThanEqual(vehiclNo: String, ticketType: TicketType, delYn: DelYn, effectDate: LocalDateTime, expireDate: LocalDateTime) : List<ProductTicket>?
    fun countByVehicleNoAndTicketTypeAndDelYnAndEffectDateLessThanEqualAndExpireDateGreaterThanEqual(vehiclNo: String, ticketType: TicketType, delYn: DelYn, effectDate: LocalDateTime, expireDate: LocalDateTime): Long
    @Query(value = "SELECT h.* from tb_seasonticket h where h.vehicleNo =:vehiclNo And h.ticket_type =:ticketType And h.del_Yn = 'N' And (h.effect_date between :effectDate and :expireDate or h.expire_date between :effectDate and :expireDate ) order by h.effect_date asc ", nativeQuery = true)
    fun findByRangedValidTickets(vehiclNo: String, ticketType: TicketType, effectDate: LocalDateTime, expireDate: LocalDateTime) : List<ProductTicket>?

}

@Repository
interface TicketClassRepository: JpaRepository<TicketClass, Long> {
    fun findByTicketNameAndDelYn(ticketname: String, delYn: DelYn): TicketClass?
}