package io.glnt.gpms.handler.calc.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.CalculationData
import io.glnt.gpms.model.entity.CgBasic
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.model.repository.CgBasicRepository
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
class FareRefService(
    private var cgBasicRepository: CgBasicRepository,
    private var calcculationData: CalculationData
) {
    companion object : KLogging()

    @Autowired
    private lateinit var fareInfoRepository: FareInfoRepository

    @Autowired
    private lateinit var farePolicyRepository: FarePolicyRepository

    fun createFareInfo(request: FareInfo): FareInfo? {
        logger.info { "createFareInfo $request" }
        try {
            fareInfoRepository.findByFareNameAndDelYn(request.fareName, YN.N)?.let {
                return null
            }
            return fareInfoRepository.saveAndFlush(request)
        }catch (e: CustomException) {
            logger.error { "fare info create failed ${request.fareName} $e" }
            return null
        }
    }

    fun createFarePolicy(request: FarePolicy): FarePolicy? {
        logger.info { "createFarePolicy $request" }
        try {
            farePolicyRepository.findByFareNameAndVehicleTypeAndDelYn(request.fareName, VehicleType.SMALL, YN.N)?.let { list ->
                list.forEach { farePolicy ->
                    request.week!!.forEach { it ->
                        if (farePolicy.week!!.contains(it)) {
//                            return CommonResult.exist(data = request, error = "fare policy create failed $request")
                            return null
                        }
                    }
                }
            }
            return farePolicyRepository.saveAndFlush(
                FarePolicy(sn = null, fareName = request.fareName, vehicleType = request.vehicleType,
                           startTime = request.startTime, endTime = request.endTime,
                           basicFareSn = request.basicFareSn, addFareSn = request.addFareSn,
                           effectDate = request.effectDate, expireDate = request.expireDate,
                           week = request.week, delYn = YN.N))
        }catch (e: CustomException) {
            logger.error { "fare policy create failed ${request.fareName} $e" }
//            return CommonResult.error("fare policy create failed ${request.fareName}")
            return null
        }
    }

    fun deleteFarePolicy(sn: Long): FarePolicy? {
        try {
            farePolicyRepository.findBySn(sn)?.let {
                it.delYn = YN.Y
                return farePolicyRepository.saveAndFlush(it)
            }
            return null
        }catch (e: CustomException) {
            logger.error { "fare policy create failed $sn $e" }
//            return CommonResult.error("fare policy create failed ${request.fareName}")
            return null
        }
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
            return CommonResult.data(fareInfoRepository.findByDelYn(YN.N))
        }catch (e: CustomException) {
            return CommonResult.error("get fare info failed $e")
        }
    }

    fun getFarePolicy(): CommonResult {
        try {
            return CommonResult.data(farePolicyRepository.findByDelYn(YN.N))
        }catch (e: CustomException) {
            return CommonResult.error("get fare info failed $e")
        }
    }

    fun getFareBasic(): CgBasic? {
        try {
            return cgBasicRepository.findByDelYn(YN.N)
        }catch (e: CustomException) {
            logger.error { "get fare basic failed $e" }
            return null
        }
    }

    fun updateFareBasic(new: CgBasic): CgBasic? {
        try {
            return cgBasicRepository.findByDelYn(YN.N)?.let { basic ->
                basic.serviceTime = new.serviceTime ?: kotlin.run { basic.serviceTime }
                basic.legTime = new.legTime ?: kotlin.run { basic.legTime }
                basic.dayMaxAmt = new.dayMaxAmt ?: kotlin.run { basic.dayMaxAmt }
                basic.effectDate = new.effectDate ?: kotlin.run { basic.effectDate }
                cgBasicRepository.saveAndFlush(basic)
            }?: kotlin.run {
                cgBasicRepository.saveAndFlush(new)
            }
        }catch (e: CustomException) {
            logger.error { "updateFareBasic failed $e" }
            return null
        }
    }

    fun init() {
        calcculationData.init()
    }
}