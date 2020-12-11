package io.glnt.gpms.handler.discount.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.discount.model.reqSearchDiscount
import io.glnt.gpms.model.repository.CorpTicketRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DiscountService {
    companion object : KLogging()

    @Autowired
    private lateinit var corpTicketRepository: CorpTicketRepository

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
}