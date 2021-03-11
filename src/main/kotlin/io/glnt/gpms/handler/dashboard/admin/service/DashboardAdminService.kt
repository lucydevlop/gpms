package io.glnt.gpms.handler.dashboard.admin.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.corp.service.CorpService
import io.glnt.gpms.handler.dashboard.admin.model.*
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.inout.service.InoutService
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.model.reqCreateProduct
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.enums.DelYn
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DashboardAdminService {
    companion object : KLogging()

    @Autowired
    lateinit var inoutService: InoutService

    @Autowired
    lateinit var parkinglotService: ParkinglotService

    @Autowired
    lateinit var facilityService: FacilityService

    @Autowired
    lateinit var relayService: RelayService

    @Autowired
    lateinit var productService: ProductService

    @Autowired
    lateinit var corpService: CorpService

    @Throws(CustomException::class)
    fun getMainGates(): CommonResult {
        try {
            val result = ArrayList<HashMap<String, Any?>>()

            val gates = parkinglotService.getParkinglotGates(reqSearchParkinglotFeature())
            when (gates.code) {
                ResultCode.SUCCESS.getCode() -> {
                    gates.data?.let {
                        val lists = gates.data as List<Gate>
                        lists.forEach {
                            if (it.delYn == DelYn.N) {
                                // gate 당 입출차 내역 조회
                                val inout = inoutService.getLastInout(it.gateType, it.gateId)
                                // 각 장비 상태 조회
                                val gateStatus = facilityService.getStatusByGate(it.gateId)
                                result.add(
                                    hashMapOf<String, Any?>(
                                        "gateId" to it.gateId,
                                        "gateName" to it.gateName,
                                        "gateType" to it.gateType,
                                        "image" to inout!!["image"],
                                        "vehicleNo" to inout["vehicleNo"],
                                        "date" to inout["date"],
                                        "carType" to inout["carType"],
                                        "breakerAction" to gateStatus!!["breakerAction"],
                                        "breakerStatus" to gateStatus["breakerStatus"],
                                        "displayStatus" to gateStatus["displayStatus"],
                                        "paystationStatus" to gateStatus["paystationStatus"],
                                        "paystationAction" to gateStatus["paystationAction"],
                                        "lprStatus" to gateStatus["lprStatus"]
                                    )
                                )
                            }
                        }
                    }
                }
            }
            return CommonResult.data(result)
        }catch (e: CustomException){
            logger.error { "Admin getMainGates failed ${e.message}" }
            return CommonResult.error("Admin getMainGates failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun getParkInLists(request: reqSearchParkin): CommonResult {
        try {
            return CommonResult.data(inoutService.getAllParkLists(request).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun getGates(): CommonResult {
        try {
            return CommonResult.data(parkinglotService.getParkinglotGates(reqSearchParkinglotFeature()).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun changeGateUse(request: reqChangeUseGate): CommonResult {
        try {
            return CommonResult.data(parkinglotService.changeDelYnGate(request.gateId, request.delYn).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun gateAction(action: String, gateId: String) : CommonResult {
        try {
            relayService.actionGate(gateId, "GATE", action)
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "Admin gateAction failed ${e.message}" }
            return CommonResult.error("Admin gateAction failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun createFacility(request: reqCreateFacility): CommonResult {
        try {
            val gate = facilityService.getGateByGateId(request.gateId)
            val result = facilityService.createFacility(
                Facility(sn = null,
                    fname = request.fname,
                    category = request.category,
                    modelid = request.modelid,
                    dtFacilitiesId = request.dtFacilitiesId,
                    facilitiesId = request.facilitiesId,
                    gateId = request.gateId,
                    udpGateid = gate!!.udpGateid,
                    ip = request.ip,
                    port = request.port,
                    sortCount = request.sortCount,
                    resetPort = request.resetPort,
                    lprType = request.lprType,
                    imagePath = request.imagePath,
                    gateType = gate.gateType
                ))
            when (result.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(result.data)
                }
                else -> {
                    return CommonResult.data()
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin createFacility failed ${e.message}" }
            return CommonResult.error("Admin createFacility failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun changeFacilityUse(request: reqChangeUseFacility): CommonResult {
        try {
            return CommonResult.data(parkinglotService.changeDelYnFacility(request.dtFacilitiesId, request.delYn).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun createMessage(request: reqCreateMessage): CommonResult {
        try {
            val data = ArrayList<reqSetDisplayMessage>()
            data.add(reqSetDisplayMessage(
                messageClass = request.messageClass,
                messageType = request.messageType,
                colorCode = request.colorCode,
                messageDesc = request.messageDesc,
                line = request.lineNumber,
                order = request.order
            ))
            val result = facilityService.setDisplayMessage(data)
            when (result.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(result.data)
                }
                else -> {
                    return CommonResult.data()
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin createMessage failed ${e.message}" }
            return CommonResult.error("Admin createMessage failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun createProductTicket(request: reqCreateProductTicket): CommonResult {
        try{
            val data = productService.createProduct(
                reqCreateProduct(sn = request.sn, vehicleNo = request.vehicleNo,
                                 effectDate = request.effectDate, expireDate = request.expireDate,
                                 userId = request.userId, gateId = request.gateId, ticketType = request.ticketType, vehicleType = request.vehicleType, corpSn = request.corpSn))
            when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(data.data)
                }
                else -> {
                    return CommonResult.data()
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin createProductTicket failed $e" }
            return CommonResult.error("Admin createProductTicket failed $e")
        }
    }

    @Throws(CustomException::class)
    fun searchCorpList(request: reqSearchCorp): CommonResult {
        try {
            val data = corpService.getCorp(request)
            when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(data.data)
                }
                else -> {
                    return CommonResult.data()
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin searchCorpList failed $e" }
            return CommonResult.error("Admin searchCorpList failed $e")
        }
    }


}