package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CorpTicketHistory
import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.enums.YN
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface CorpTicketRepository: JpaRepository<CorpTicketInfo, Long> {
    fun findBySn(sn: Long): CorpTicketInfo
    fun findByCorpSn(corpSn: Long) : List<CorpTicketInfo>
    fun findByCorpSnAndDelYn(corpSn: Long, delYn: YN): List<CorpTicketInfo>?
    fun findByCorpSnAndClassSnAndDelYn(corpSn: Long, classSn: Long, delYn: YN): CorpTicketInfo?
//    fun findByCorpSnAndDiscountClassSnAndDelYn(corpSn: Long, discountClassSn: Long, delYn: DelYn): CorpTicketInfo?
    fun findBySnAndDelYn(sn: Long, delYn: YN): CorpTicketInfo?
//    fun findByCorpIdAndExpireDateGreaterThanEqualAndAbleCntIsGreaterThan(corpId: String, date: LocalDateTime, cnt: Int): List<CorpTicket>?
//    fun findTopByCorpSnAndCorpClassSnAndDelYnAndAbleCntIsGreaterThanOrderByCreateDateAsc(corpSn: Long, corpClassNo: Long, delYn: DelYn, ableCnt: Int): CorpTicket?
}

@Repository
interface CorpTicketHistoryRepository: JpaRepository<CorpTicketHistory, Long> {
    @Query(value = "SELECT h.* from tb_corp_ticket_history h where h.ticket_sn =:ticketSn And h.total_quantity > h.use_quantity And h.del_Yn = 'N' order by h.create_date Limit 1", nativeQuery = true)
    fun findTopByTicket(ticketSn: Long): CorpTicketHistory?
    fun findByTicketSnAndDelYn(ticketSn: Long, delYn: YN): List<CorpTicketHistory>?
    fun findBySnAndDelYn(sn: Long, delYn: YN): CorpTicketHistory?
}

@Repository
interface DiscountClassRepository: JpaRepository<DiscountClass, Long>{
    fun findByExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(start: LocalDateTime, end: LocalDateTime, delYn: YN): List<DiscountClass>?
    fun findByDelYn(delYn: YN): List<DiscountClass>?
    fun findBySn(sn: Long): DiscountClass
}

@Repository
interface InoutDiscountRepository: JpaRepository<InoutDiscount, Long>, JpaSpecificationExecutor<InoutDiscount> {
    fun findBySnAndDelYn(Sn: Long, delYn: YN): InoutDiscount?
    fun findByTicketHistSn(ticketSn: Long): List<InoutDiscount>?
    fun findByInSnAndDelYn(inSn: Long, delYn: YN): List<InoutDiscount>?
    fun findByTicketHistSnAndInSnAndDelYn(ticketHistSn: Long, inSn: Long, delYn: YN): List<InoutDiscount>?
    fun findByTicketHistSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(ticketHistSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, YN: YN): List<InoutDiscount>?
    fun findByInSnAndDiscountClassSnAndDelYn(inSn: Long, discountClassSn: Long, YN: YN): List<InoutDiscount>?
    fun findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(discountClassSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, YN: YN): List<InoutDiscount>?
    fun findByCorpSnAndDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(corpSn: Long, discountClassSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, YN: YN): List<InoutDiscount>?
    fun findByCorpSnAndTicketClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(corpSn: Long, discountClassSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, YN: YN): List<InoutDiscount>?
    fun findByInSnAndDelYnAndCalcYn(inSn: Long, YN: YN, calcYn: YN): List<InoutDiscount>?
//    fun findAll(specification: Specification<InoutDiscount>): List<InoutDiscount>?
}