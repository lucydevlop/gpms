package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.dto.CorpTicketClassDTO
import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.repository.InoutDiscountRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class InoutDiscountService(
    private val inoutDiscountRepository: InoutDiscountRepository
) {
    companion object : KLogging()

    fun ableDiscountCntByInSn(inSn: Long, corpTicketClassDTO: CorpTicketClassDTO): Int? {
        val result = ArrayList<Long>()

        inoutDiscountRepository.findByInSnAndDiscountClassSnAndDelYn(inSn, corpTicketClassDTO.discountClass!!.sn!!, DelYn.N)?.sumBy { it -> it.quantity!! }.also {
            if (it!! > corpTicketClassDTO.onceMax!!) return 0
            if (it == 0) result.add(corpTicketClassDTO.onceMax!!) else  result.add(corpTicketClassDTO.onceMax!!-it)
        }
        inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
            corpTicketClassDTO.discountClass!!.sn!!, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(
                DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
            if (it!! > corpTicketClassDTO.dayMax!!) return 0
            if (it == 0) result.add(corpTicketClassDTO.dayMax!!) else  result.add(corpTicketClassDTO.dayMax!!-it)
        }
        inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
            corpTicketClassDTO.discountClass!!.sn!!, DateUtil.firstDayToLocalDateTime(DateUtil.nowDate), DateUtil.lastDayToLocalDateTime(
                DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
            if (it!! > corpTicketClassDTO.monthMax!!) return 0
            if (it == 0) result.add(corpTicketClassDTO.monthMax!!) else  result.add(corpTicketClassDTO.monthMax!!-it)
        }

        return result.minOrNull()?.toInt()
    }
}