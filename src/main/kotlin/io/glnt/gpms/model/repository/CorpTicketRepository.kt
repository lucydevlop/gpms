package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CorpTicketHistory
import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.enums.DelYn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface CorpTicketRepository: JpaRepository<CorpTicketInfo, Long> {
    fun findByCorpSn(corpSn: Long) : List<CorpTicketInfo>?
    fun findByCorpSnAndDelYn(corpSn: Long, delYn: DelYn): List<CorpTicketInfo>?
    fun findByCorpSnAndDiscountClassSnAndDelYn(corpSn: Long, discountClassSn: Long, delYn: DelYn): CorpTicketInfo?
//    fun findByCorpIdAndExpireDateGreaterThanEqualAndAbleCntIsGreaterThan(corpId: String, date: LocalDateTime, cnt: Int): List<CorpTicket>?
//    fun findTopByCorpSnAndCorpClassSnAndDelYnAndAbleCntIsGreaterThanOrderByCreateDateAsc(corpSn: Long, corpClassNo: Long, delYn: DelYn, ableCnt: Int): CorpTicket?
}

@Repository
interface CorpTicketHistoryRepository: JpaRepository<CorpTicketHistory, Long> {
    @Query(value = "SELECT h.* from tb_corp_ticket_history h where h.ticket_sn =:ticketSn And h.total_quantity > h.use_quantity And h.del_Yn = 'N' order by h.create_date Limit 1", nativeQuery = true)
    fun findTopByTicket(ticketSn: Long): CorpTicketHistory?
    fun findByTicketSnAndDelYn(ticketSn: Long, delYn: DelYn): List<CorpTicketHistory>?
}

@Repository
interface DiscountClassRepository: JpaRepository<DiscountClass, Long>{
    fun findByExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(start: LocalDateTime, end: LocalDateTime, delYn: DelYn): List<DiscountClass>?
    fun findByDelYn(delYn: DelYn): List<DiscountClass>?
    fun findBySn(sn: Long): DiscountClass
}

@Repository
interface InoutDiscountRepository: JpaRepository<InoutDiscount, Long> {
    fun findByTicketHistSn(ticketSn: Long): List<InoutDiscount>?
    fun findByInSnAndDelYn(inSn: Long, delYn: DelYn): List<InoutDiscount>?
    fun findByTicketHistSnAndInSnAndDelYn(ticketHistSn: Long, inSn: Long, delYn: DelYn): List<InoutDiscount>?
    fun findByTicketHistSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(ticketHistSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, delYn: DelYn): List<InoutDiscount>?
    fun findByInSnAndDiscountClassSnAndDelYn(inSn: Long, discountClassSn: Long, delYn: DelYn): List<InoutDiscount>?
    fun findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(discountClassSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, delYn: DelYn): List<InoutDiscount>?
}