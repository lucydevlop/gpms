package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.CorpTicket
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.enums.DelYn
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime

@Repository
interface CorpTicketRepository: JpaRepository<CorpTicket, Long> {
    fun findByCorpId(corpId: String) : List<CorpTicket>?
    fun findByCorpIdAndExpireDateGreaterThanEqualAndAbleCntIsGreaterThan(corpId: String, date: LocalDateTime, cnt: Int): List<CorpTicket>?
    fun findTopByCorpSnAndCorpClassSnAndDelYnAndAbleCntIsGreaterThanOrderByCreateDateAsc(corpSn: Long, corpClassNo: Long, delYn: DelYn, ableCnt: Int): CorpTicket?
}

@Repository
interface DiscountClassRepository: JpaRepository<DiscountClass, Long>{
    fun findByExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(start: LocalDateTime, end: LocalDateTime, delYn: DelYn): List<DiscountClass>?
    fun findByDelYn(delYn: DelYn): List<DiscountClass>?
    fun findBySn(sn: Long): DiscountClass
}

@Repository
interface InoutDiscountRepository: JpaRepository<InoutDiscount, Long> {
    fun findByTicketSn(ticketSn: Long): List<InoutDiscount>?
    fun findByTicketSnAndInSnAndDelYn(ticketSn: Long, inSn: Long, delYn: DelYn): List<InoutDiscount>?
    fun findByTicketSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(ticketSn: Long, startDate: LocalDateTime, endDate: LocalDateTime, delYn: DelYn): List<InoutDiscount>?
}