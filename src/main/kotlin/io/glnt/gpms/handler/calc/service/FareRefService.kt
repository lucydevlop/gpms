package io.glnt.gpms.handler.calc.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.model.enums.WeekType
import io.glnt.gpms.model.repository.FareInfoRepository
import io.glnt.gpms.model.repository.FarePolicyRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import javax.persistence.criteria.CriteriaBuilder
import javax.persistence.criteria.CriteriaQuery
import javax.persistence.criteria.Root


@Service
class FareRefService {
    companion object : KLogging()

    @Autowired
    private lateinit var fareInfoRepository: FareInfoRepository

    @Autowired
    private lateinit var farePolicyRepository: FarePolicyRepository

    fun createFareInfo(request: FareInfo): CommonResult {
        logger.info { "createFareInfo $request" }
        try {
            fareInfoRepository.findByFareNameAndDelYn(request.fareName, DelYn.N)?.let {
                return CommonResult.exist(data = request, error = "fare info create failed ${request.fareName}")
            }
            fareInfoRepository.save(request)
        }catch (e: CustomException) {
            return CommonResult.error("fare info create failed ${request.fareName}")
        }
        return CommonResult.created()
    }

    fun createFarePolicy(request: FarePolicy): CommonResult {
        logger.info { "createFarePolicy $request" }
        try {
            farePolicyRepository.findByFareNameAndVehicleTypeAndDelYn(request.fareName, VehicleType.SMALL, DelYn.N)?.let { list ->
                list.forEach { farePolicy ->
                    request.week!!.forEach { it ->
                        if (farePolicy.week!!.contains(it)) {
                            return CommonResult.exist(data = request, error = "fare policy create failed $request")
                        }
                    }
                }
            }
            farePolicyRepository.save(
                FarePolicy(sn = null, fareName = request.fareName, vehicleType = request.vehicleType,
                           startTime = request.startTime, endTime = request.endTime,
                           basicFareSn = request.basicFareSn, addFareSn = request.addFareSn,
                           effectDate = request.effectDate, expireDate = request.expireDate,
                           week = request.week, delYn = DelYn.N))
        }catch (e: CustomException) {
            return CommonResult.error("fare policy create failed ${request.fareName}")
        }
        return CommonResult.created()
    }

    fun findAllFarePolicySpecification(key: String, `val`: String): Specification<FarePolicy?>? {
        return Specification<FarePolicy?> { root: Root<FarePolicy?>, query: CriteriaQuery<*>?, cb: CriteriaBuilder ->
            cb.like(
                cb.function(
                    "JSON_EXTRACT",
                    String::class.java, root.get<Any>("week"), cb.literal("$")
                ),
                "%$`val`%"
            )
        }
    }

    fun getFareInfo(): CommonResult {
        try {
            return CommonResult.data(fareInfoRepository.findByDelYn(DelYn.N))
        }catch (e: CustomException) {
            return CommonResult.error("get fare info failed $e")
        }
    }

    fun getFarePolicy(): CommonResult {
        try {
            return CommonResult.data(farePolicyRepository.findByDelYn(DelYn.N))
        }catch (e: CustomException) {
            return CommonResult.error("get fare info failed $e")
        }
    }
}