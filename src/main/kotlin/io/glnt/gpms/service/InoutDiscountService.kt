package io.glnt.gpms.service

import io.glnt.gpms.common.utils.DateUtil
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

    fun ableDiscountCntByInSn(inSn: Long, discountClass: DiscountClassDTO): Int? {
        val result = ArrayList<Int>()

        inoutDiscountRepository.findByInSnAndDiscountClassSnAndDelYn(inSn, discountClass.sn!!, DelYn.N)?.sumBy { it -> it.quantity!! }.also {
            if (it!! > discountClass.disMaxNo!!) return 0
            if (it == 0) result.add(discountClass.disMaxNo!!) else  result.add(discountClass.disMaxNo!!-it)
        }
        inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
            discountClass.sn!!, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(
                DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
            if (it!! > discountClass.disMaxDay!!) return 0
            if (it == 0) result.add(discountClass.disMaxDay!!) else  result.add(discountClass.disMaxDay!!-it)
        }
        inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
            discountClass.sn!!, DateUtil.firstDayToLocalDateTime(DateUtil.nowDate), DateUtil.lastDayToLocalDateTime(
                DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
            if (it!! > discountClass.disMaxMonth!!) return 0
            if (it == 0) result.add(discountClass.disMaxMonth!!) else  result.add(discountClass.disMaxMonth!!-it)
        }

        return result.minOrNull()
    }

}