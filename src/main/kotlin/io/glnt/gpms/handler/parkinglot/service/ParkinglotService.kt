package io.glnt.gpms.handler.parkinglot.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.model.reqAddParkinglotFeature
import io.glnt.gpms.model.entity.ParkFeature
import io.glnt.gpms.model.repository.ParkFeatureRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import java.lang.RuntimeException

@Service
class ParkinglotService {
    companion object : KLogging()

    @Autowired
    private lateinit var parkFeatureRepository: ParkFeatureRepository

    fun addParkinglotFeature(request: reqAddParkinglotFeature): CommonResult = with(request) {
        logger.debug("addParkinglotFeature service {}", request)
        try {
            val new = ParkFeature(
                idx = null,
                featureId = featureId,
                flag = flag,
                groupKey = groupKey,
                category = category,
                connectionType = connetionType,
                ip = ip,
                port = port,
                originImgPath = path,
                transactinoId = transactionId
            )
            parkFeatureRepository.save(new)
            return CommonResult.created("parkinglot feature add success")
        } catch (e: RuntimeException) {
            logger.error("addParkinglotFeature error {} ", e.message)
            return CommonResult.error("parkinglot feature db add failed ")
        }
    }

    fun getParkinglotFeature(requet: reqSearchParkinglotFeature): CommonResult {
        requet.gatewayKey?.let {
            val lists = parkFeatureRepository.findByGroupKey(it)
            return if (lists.isNullOrEmpty()) CommonResult.notfound("parkinglot feature") else CommonResult.data(lists)
        } ?: run {
            parkFeatureRepository.findAll().let {
                return CommonResult.data(it)
            }
        }
    }
}