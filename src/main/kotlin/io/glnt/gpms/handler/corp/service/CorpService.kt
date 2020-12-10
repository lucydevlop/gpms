package io.glnt.gpms.io.glnt.gpms.handler.corp.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.io.glnt.gpms.handler.corp.model.reqSearchCorp
import io.glnt.gpms.model.repository.CorpRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class CorpService {
    companion object : KLogging()

    @Autowired
    private lateinit var corpRepository: CorpRepository

    fun getCorp(request: reqSearchCorp): CommonResult {
        request.corpId?.let {
            val list = corpRepository.findByCorpId(it)
            return if (list == null) CommonResult.notfound("corp") else CommonResult.data(list)
        } ?: run {
            corpRepository.findAll().let {
                return CommonResult.data(it)
            }
        }
    }


}