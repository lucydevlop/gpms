package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.SeasonTicket
import io.glnt.gpms.model.entity.TicketClass
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface SeasonTicketRepository: JpaRepository<SeasonTicket, Long>, JpaSpecificationExecutor<SeasonTicket> {
    //fun findByVehicleNoAndValidDateGreaterThanEqualAndRegDateLessThanEqualAndDelYn(vehiclNo: String, date1: LocalDateTime, date2: LocalDateTime, delYn: DelYn): SeasonTicket?
    fun findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(vehiclNo: String, date1: LocalDateTime, date2: LocalDateTime, delYn: DelYn): List<SeasonTicket>?
    fun findByVehicleNoAndValidDateGreaterThanEqualAndDelYn(vehiclNo: String, date1: LocalDateTime, delYn: DelYn): SeasonTicket?
//    fun findAll(specification: Specification<SeasonTicket>): List<SeasonTicket>?
    fun findBySn(sn: Long): SeasonTicket?
    fun findByVehicleNoAndEffectDateAndExpireDateAndTicketTypeAndDelYn(vehiclNo: String, effectDate: LocalDateTime, expireDate: LocalDateTime, ticketType: TicketType, delYn: DelYn): SeasonTicket?
    fun findByVehicleNoAndTicketTypeAndDelYnAndEffectDateGreaterThanAndExpireDateLessThanEqual(vehiclNo: String, ticketType: TicketType, delYn: DelYn, effectDate: LocalDateTime, expireDate: LocalDateTime) : List<SeasonTicket>?
    fun countByVehicleNoAndTicketTypeAndDelYnAndEffectDateLessThanEqualAndExpireDateGreaterThanEqual(vehiclNo: String, ticketType: TicketType, delYn: DelYn, effectDate: LocalDateTime, expireDate: LocalDateTime): Long
//    @Query(value = "SELECT h.* from tb_seasonticket h where h.vehicleNo =:vehiclNo And h.ticket_type =:ticketType And h.del_Yn = 'N' And (h.effect_date between :effectDate and :expireDate or h.expire_date between :effectDate and :expireDate ) order by h.effect_date asc ", nativeQuery = true)
//fun findByRangedValidTickets(vehicleNo: String, ticketType: TicketType, effectDate: LocalDateTime, expireDate: LocalDateTime) : List<ProductTicket>?
    @Query(value = "SELECT h.* from tb_seasonticket h where h.vehicleNo =:vehicleNo And h.ticket_type =:ticketType And h.del_yn = 'N' And (h.effect_date between :effectDate and :expireDate ) order by h.effect_date asc ", nativeQuery = true)
    fun findByRangedValidTickets(vehicleNo: String, ticketType: String, effectDate: LocalDateTime, expireDate: LocalDateTime) : List<SeasonTicket>?

}

@Repository
interface TicketClassRepository: JpaRepository<TicketClass, Long> {
    fun findBySn(sn: Long): Optional<TicketClass>
    fun findByTicketNameAndDelYn(ticketname: String, delYn: DelYn): TicketClass?
}