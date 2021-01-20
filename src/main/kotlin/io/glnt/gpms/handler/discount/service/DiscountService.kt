package io.glnt.gpms.handler.discount.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.discount.model.reqSearchDiscount
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.repository.CorpTicketRepository
import io.glnt.gpms.model.repository.DiscountClassRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DiscountService {
    companion object : KLogging()

    @Autowired
    private lateinit var discountClassRepository: DiscountClassRepository

    @Autowired
    private lateinit var corpTicketRepository: CorpTicketRepository

    fun getDiscountClass() : CommonResult {
        return CommonResult.data(discountClassRepository.findAll())
    }

    fun getByCorp(request: reqSearchDiscount) : CommonResult {
        request.corpId?.let {
            val lists = corpTicketRepository.findByCorpId(it)
            return if (lists.isNullOrEmpty()) CommonResult.notfound("corp ticket") else CommonResult.data(lists)
        } ?: run {
            corpTicketRepository.findAll().let {
                return CommonResult.data(it)
            }
        }
    }

    fun createDiscountClass(request: DiscountClass): CommonResult {
        logger.info { "createDiscountClass $request" }
        try {
            discountClassRepository.save(request)
            return CommonResult.data(getDiscountClass())
        }catch (e: RuntimeException) {
            logger.error { "createDiscountClass error ${e.message}" }
            return CommonResult.Companion.error("tb_corpclass create failed")
        }
    }
}