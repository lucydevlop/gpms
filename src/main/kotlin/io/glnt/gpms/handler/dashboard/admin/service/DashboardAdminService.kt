package io.glnt.gpms.handler.dashboard.admin.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.handler.dashboard.admin.model.*
import io.glnt.gpms.handler.facility.model.reqSetDisplayMessage
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.user.service.AuthService
import io.glnt.gpms.handler.file.service.ExcelUploadService
import io.glnt.gpms.common.api.ResetClient
import io.glnt.gpms.model.dto.request.*
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.service.*
import io.reactivex.Observable
import io.reactivex.Single
import mu.KLogging
import org.apache.http.HttpStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.util.concurrent.TimeUnit


@Service
class DashboardAdminService(
    private var restAPIManager: RestAPIManagerUtil,
    private var corpService: CorpService,
    private var resetClient: ResetClient
) {
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
    lateinit var discountService: DiscountService

    @Autowired
    lateinit var excelService: ExcelUploadService

    @Autowired
    lateinit var userService: AuthService

    @Autowired
    lateinit var fareRefService: FareRefService

    @Autowired
    lateinit var gateService: GateService

    @Throws(CustomException::class)
    fun getMainGates(): CommonResult {
        try {
            val result = ArrayList<HashMap<String, Any?>>()

            gateService.findActiveGate().let { gates ->
                gates.forEach {
                    // gate 당 입출차 내역 조회
                    val inout = inoutService.getLastInout(it.gateType!!, it.gateId?: "")

                    if (it.gateType!! == GateTypeStatus.IN_OUT) {
                        // 각 장비 상태 조회
                        val gateStatus = facilityService.getStatusByINOUTGate(it.gateId?: "")
                        result.add(
                            hashMapOf<String, Any?>(
                                "gateId" to it.gateId,
                                "gateName" to it.gateName,
                                "gateType" to it.gateType,
                                "image" to inout!!["image"],
                                "vehicleNo" to inout["vehicleNo"],
                                "date" to inout["date"],
                                "carType" to inout["carType"],
                                "outImage" to inout["outImage"],
                                "outVehicleNo" to inout["outVehicleNo"],
                                "outDate" to inout["outDate"],
                                "outCarType" to inout["outCarType"],
                                "breakerAction" to gateStatus!!["breakerAction"],
                                "breakerStatus" to gateStatus["breakerStatus"],
                                "inDisplayStatus" to gateStatus["inDisplayStatus"],
                                "outDisplayStatus" to gateStatus["outDisplayStatus"],
                                "paystationStatus" to gateStatus["paystationStatus"],
                                "paystationAction" to gateStatus["paystationAction"],
                                "inLprStatus" to gateStatus["inLprStatus"],
                                "outLprStatus" to gateStatus["outLprStatus"]
                            )
                        )
                    } else {
                        // 각 장비 상태 조회
                        val gateStatus = facilityService.getStatusByGate(it.gateId?: "")

                        result.add(
                            hashMapOf<String, Any?>(
                                "gateId" to it.gateId,
                                "gateName" to it.gateName,
                                "gateType" to it.gateType,
                                "image" to inout!!["image"],
                                "vehicleNo" to inout["vehicleNo"],
                                "date" to inout["date"],
                                "carType" to inout["carType"],
                                "outImage" to inout["outImage"],
                                "outVehicleNo" to inout["outVehicleNo"],
                                "outDate" to inout["outDate"],
                                "outCarType" to inout["outCarType"],
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



//            when (gates.code) {
//                ResultCode.SUCCESS.getCode() -> {
//                    gates.data?.let {
//                        val lists = gates.data as List<Gate>
//                        lists.forEach {
//                            if (it.delYn == DelYn.N) {
//                                // gate 당 입출차 내역 조회
//                                val inout = inoutService.getLastInout(it.gateType, it.gateId)
//                                // 각 장비 상태 조회
//                                val gateStatus = facilityService.getStatusByGate(it.gateId)
//                                result.add(
//                                    hashMapOf<String, Any?>(
//                                        "gateId" to it.gateId,
//                                        "gateName" to it.gateName,
//                                        "gateType" to it.gateType,
//                                        "image" to inout!!["image"],
//                                        "vehicleNo" to inout["vehicleNo"],
//                                        "date" to inout["date"],
//                                        "carType" to inout["carType"],
//                                        "outImage" to inout["outImage"],
//                                        "outVehicleNo" to inout["outVehicleNo"],
//                                        "outDate" to inout["outDate"],
//                                        "outCarType" to inout["outCarType"],
//                                        "breakerAction" to gateStatus!!["breakerAction"],
//                                        "breakerStatus" to gateStatus["breakerStatus"],
//                                        "displayStatus" to gateStatus["displayStatus"],
//                                        "paystationStatus" to gateStatus["paystationStatus"],
//                                        "paystationAction" to gateStatus["paystationAction"],
//                                        "lprStatus" to gateStatus["lprStatus"]
//                                    )
//                                )
//                            }
//                        }
//                    }
//                }
//            }
            return CommonResult.data(result)
        }catch (e: CustomException){
            logger.error { "Admin getMainGates failed ${e.message}" }
            return CommonResult.error("Admin getMainGates failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun getParkInLists(request: reqSearchParkin): CommonResult {
        try {
            val results = inoutService.getAllParkLists(request)

            results?.let { result ->
                request.searchDateLabel?.let { label ->
                    when (label) {
                        DisplayMessageClass.IN -> result.sortedByDescending { it.inDate }
                        DisplayMessageClass.OUT -> result.sortedByDescending { it.outDate }
                        else -> result
                    }
                }
            }
            return CommonResult.data(results)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }
    @Throws(CustomException::class)
    fun createInout(request: resParkInList): CommonResult {
        try {
            return CommonResult.data(inoutService.createInout(request).data)
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun editParkInout(request: resParkInList): CommonResult {
        try {
            return CommonResult.data(inoutService.updateInout(request))
        }catch (e: CustomException){
            logger.error { "Admin getParkInLists failed ${e.message}" }
            return CommonResult.error("Admin getParkInLists failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun deleteParkInout(sn: Long): CommonResult {
        try {
            return CommonResult.data(inoutService.deleteInout(sn).data)
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
    fun getGateGroups(): CommonResult {
        try {
            return CommonResult.data(gateService.getGateGroups())
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
            relayService.actionGate(gateId, "GATE", action, "manual")
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "Admin gateAction failed ${e.message}" }
            return CommonResult.error("Admin gateAction failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun gateResetAction(gateId: String, category: FacilityCategoryType) : CommonResult {
        try {
            parkinglotService.getGate(gateId)?.let { gate ->
                facilityService.getOneFacilityByGateIdAndCategory(gateId, category)?.let { facility ->
                    facility.resetPort?.let { it ->
                        val port = it.toInt()-1
                        if (port < 0) return CommonResult.error("Admin gateResetAction failed")
                        val url = gate.resetSvr+port
                        resetClient.sendReset(url).let { response ->
                            singleTimer()
                            logger.warn { "RESET ${facility.dtFacilitiesId} response ${response!!.status} ${response.body.toString()}" }
                            if (response!!.status == HttpStatus.SC_OK) {
                                Observable.timer(2, TimeUnit.SECONDS).subscribe {
                                    logger.info { "reset one more $url" }
                                    resetClient.sendReset(url).let { reResponse ->
                                        logger.info { "RESET ${facility.dtFacilitiesId} response ${reResponse!!.status} ${reResponse.body.toString()}" }
                                    }
                                }

                            }

                        }
                    }
                }
            }
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
                    fName = request.fName,
                    category = request.category,
                    modelId = request.modelId,
                    dtFacilitiesId = request.dtFacilitiesId,
                    facilitiesId = request.facilitiesId,
                    gateId = request.gateId,
                    udpGateId = gate!!.udpGateId,
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
    fun createMessage(request: ReqCreateMessage): CommonResult {
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
    fun updateMessage(request: ArrayList<ReqCreateMessage>): CommonResult {
        try {
            val result = facilityService.updateDisplayMessage(request)
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
    fun getDisplayInfo(): CommonResult {
        try {
            return facilityService.getDisplayInfo()?.let {
                relayService.sendDisplayInfo()
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.notfound("Admin getDisplayInfo not found")
            }
        }catch (e: CustomException){
            logger.error { "Admin getDisplayInfo failed $e" }
            return CommonResult.error("Admin getDisplayInfo failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun updateDisplayInfo(request: reqDisplayInfo): CommonResult {
        try {
            return facilityService.updateDisplayInfo(request)?.let {
                relayService.sendUpdateDisplayInfo(it)
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.error("Admin updateDisplayInfo failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin updateDisplayInfo failed $e" }
            return CommonResult.error("Admin updateDisplayInfo failed ${e.message}")
        }
    }

    @Throws(CustomException::class)
    fun createProductTicket(request: reqCreateProductTicket): CommonResult {
        try{
            val data = productService.createProduct(request)
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
    fun createProductTickets(request: ArrayList<reqCreateProductTicket>): CommonResult {
        try{
            request.forEach{ it ->
                it.corpName?.let { corpName ->
                    corpService.getStoreByCorpName(corpName)?.let { corp ->
                        it.corpSn = corp.sn
                    }
                }
//                if (it.corpName!!.isNotEmpty()) {
//                    corpService.getStoreByCorpName(it.corpName!!).ifPresent { corp ->
//                        it.corpSn = corp.sn
//                    }
//                }
                createProductTicket(it)

//                createProductTicket(
//                    reqCreateProductTicket(corpSn = corpSn, vehicleNo = it.vehicleNo, effectDate = it.effectDate, expireDate = it.expireDate,
//                        ticketType = it.ticketType, name = it.name, tel = it.tel,
//                        vehiclekind = it.vehiclekind, etc = it.etc, etc1 = it.etc1, vehicleType = it.vehicleType
//                    )
//                )
            }
        }catch (e: CustomException){
            logger.error { "Admin createProductTicket failed $e" }
            return CommonResult.error("Admin createProductTicket failed $e")
        }
        return CommonResult.data()
    }

//    @Throws(CustomException::class)
//    fun searchCorpList(request: reqSearchCorp): CommonResult {
//        try {
//            val data = corpService.getCorp(request)
//            when (data.code) {
//                ResultCode.SUCCESS.getCode() -> {
//                    return CommonResult.data(data.data)
//                }
//                else -> {
//                    return CommonResult.data()
//                }
//            }
//        }catch (e: CustomException){
//            logger.error { "Admin searchCorpList failed $e" }
//            return CommonResult.error("Admin searchCorpList failed $e")
//        }
//    }

    @Throws(CustomException::class)
    fun createCorpTicket(request: reqCreateCorpTicket): CommonResult {
        try {
            val data = discountService.createCorpTicket(request)
            when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(data.data)
                }
                else -> {
                    return CommonResult.data()
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin createCorpTicket failed $e" }
            return CommonResult.error("Admin createCorpTicket failed $e")
        }
    }

    @Throws(CustomException::class)
    fun deleteCorpTicket(request: Long) : CommonResult {
        try {
            return discountService.deleteCorpTicket(request)?.let {
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.error("deleteCorpTicket failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin deleteCorpTicket failed $e" }
            return CommonResult.error("Admin deleteCorpTicket failed $e")
        }
    }

    @Throws(CustomException::class)
    @Transactional
    fun createProductTicketByFiles(file: MultipartFile): CommonResult {
        try{
            //todo validate
            val data = excelService.loadExcel(file, "SEASONTICKET")
            when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    return CommonResult.data(data.data)
                }
                else -> {
                    return CommonResult.error("file upload failed")
                }
            }

        } catch (e: CustomException){
            logger.error { "Admin createProductTicketByFiles failed $e" }
            return CommonResult.error("Admin createProductTicketByFiles failed $e")
        }
    }

    @Throws(CustomException::class)
    @Transactional
    fun createTemplateOfProductTicket(): CommonResult {
        try{
            productService.getProducts(reqSearchProductTicket(searchLabel = "", searchText = ""))?.let { tickets ->
                return CommonResult.data(excelService.downloadTemplateOfProductTicket(tickets))
            } ?: kotlin.run {
                return CommonResult.error("file upload failed")
            }
//            when (data.code) {
//                ResultCode.SUCCESS.getCode() -> {
//                    return CommonResult.data(excelService.downloadTemplateOfProductTicket(data.data as List<ProductTicket>))
//                }
//                else -> {
//                    return CommonResult.error("file upload failed")
//                }
//            }

        } catch (e: CustomException){
            logger.error { "Admin createProductTicketByFiles failed $e" }
            return CommonResult.error("Admin createProductTicketByFiles failed $e")
        }
    }

    @Throws(CustomException::class)
    fun searchProductTicket(request: reqSearchProductTicket): CommonResult {
        try{
            return CommonResult.data(productService.getProducts(request))
        } catch (e: CustomException) {
            logger.error { "Admin searchProductTicket failed $e" }
            return CommonResult.error("Admin searchProductTicket failed $e")
        }
    }

    @Throws(CustomException::class)
    fun searchAdminUsers(request: reqSearchItem): CommonResult {
        try{
            val roles = arrayListOf<UserRole>(UserRole.ADMIN, UserRole.SUPER_ADMIN, UserRole.OPERATION)
            val data = userService.searchUsers(reqSearchItem(searchLabel = request.searchLabel, searchText = request.searchText, searchRoles = roles))
            return when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    CommonResult.data(data.data)
                }
                else -> {
                    CommonResult.error("searchAdminUsers failed")
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin searchAdminUsers failed $e" }
            return CommonResult.error("Admin searchAdminUsers failed $e")
        }
    }

    @Throws(CustomException::class)
    fun deleteAdminUser(sn: Long): CommonResult {
        try {
            val data = userService.deleteUser(sn)
            return when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    CommonResult.data(data.data)
                }
                else -> {
                    CommonResult.error("deleteAdminUser failed")
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin deleteAdminUser failed $e" }
            return CommonResult.error("Admin deleteAdminUser failed $e")
        }
    }

    @Throws(CustomException::class)
    fun editAdminUser(request: reqUserInfo): CommonResult {
        try {
            return userService.editUser(request)?.let {
                CommonResult.data(it)
            }?: run {
                CommonResult.error("Admin editAdminUser failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin editAdminUser failed $e" }
            return CommonResult.error("Admin editAdminUser failed $e")
        }
    }

    @Transactional(readOnly = true)
    @Throws(CustomException::class)
    fun getFareBasic() : CommonResult {
        try {
            return fareRefService.getFareBasic()?.let {
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.notfound("cgBasic not found")
            }
        }catch (e: CustomException){
            logger.error { "Admin getFareBasic failed $e" }
            return CommonResult.error("Admin getFareBasic failed $e")
        }
    }

    @Transactional(readOnly = true)
    @Throws(CustomException::class)
    fun getFareInfo() : CommonResult {
        try {
            val data = fareRefService.getFareInfo()
            return when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    CommonResult.data(data.data)
                }
                else -> {
                    CommonResult.error("getFareInfo failed")
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin getFareInfo failed $e" }
            return CommonResult.error("Admin getFareInfo failed $e")
        }
    }

    @Transactional(readOnly = true)
    @Throws(CustomException::class)
    fun getFarePolicy() : CommonResult {
        try {
            val data = fareRefService.getFarePolicy()
            return when (data.code) {
                ResultCode.SUCCESS.getCode() -> {
                    CommonResult.data(data.data)
                }
                else -> {
                    CommonResult.error("getFarePolicy failed")
                }
            }
        }catch (e: CustomException){
            logger.error { "Admin getFarePolicy failed $e" }
            return CommonResult.error("Admin getFarePolicy failed $e")
        }
    }

    @Throws(CustomException::class)
    fun updateFareBasic(request: reqFareBasic) : CommonResult {
        try {
            return fareRefService.updateFareBasic(CgBasic(
                sn = null,
                serviceTime = request.serviceTime,
                legTime = request.regTime,
                effectDate = request.effectDate,
                delYn = YN.N,
                dayMaxAmt = request.dayMaxAmt)
            )?.let {
                fareRefService.init()
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.error("Admin updateFareBasic failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin updateFareBasic failed $e" }
            return CommonResult.error("Admin updateFareBasic failed $e")
        }
    }

    @Throws(CustomException::class)
    fun createFareInfo(request: reqFareInfo) : CommonResult {
        try {
            return fareRefService.createFareInfo(
                FareInfo(sn = null, fareName = request.fareName, type = request.type,
                         time1 = request.time1, won1 = request.won1, count = request.count, count1 = request.count1, delYn = YN.N))?.let {
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.Companion.error("Admin createFareInfo failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin createFareInfo failed $e" }
            return CommonResult.error("Admin createFareInfo failed $e")
        }
    }

    @Throws(CustomException::class)
    fun createFarePolicy(request: reqFarePolicy) : CommonResult {
        try {
            return fareRefService.createFarePolicy(
                FarePolicy(sn = null, fareName = request.fareName, vehicleType = request.vehicleType,
                    startTime = request.startTime, endTime = request.endTime, basicFareSn = request.basicFareSn, addFareSn = request.addFareSn,
                    effectDate = request.effectDate, expireDate = request.expireDate,
                    week = request.week, delYn = YN.N ))?.let {
                fareRefService.init()
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.Companion.error("Admin createFarePolicy failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin createFarePolicy failed $e" }
            return CommonResult.error("Admin createFarePolicy failed $e")
        }
    }

    @Throws(CustomException::class)
    fun deleteFarePolicy(request: Long) : CommonResult {
        try {
            return fareRefService.deleteFarePolicy(request)?.let {
                CommonResult.data(it)
            }?: kotlin.run {
                CommonResult.Companion.error("deleteFarePolicy failed $request")
            }
        }catch (e: CustomException){
            logger.error { "Admin deleteFarePolicy failed $e" }
            return CommonResult.error("Admin deleteFarePolicy failed $e")
        }
    }

    fun createDiscountTicket(request: reqDiscountTicket): CommonResult {
        try {
            discountService.createDiscountClass(
                DiscountClass(sn = null, discountNm = request.discountNm,
                              dayRange = request.dayRange, unit = request.unitTime!!,
                              disUse = request.disUse, disMaxNo = request.disMaxNo,
                              disMaxDay = request.disMaxDay,  disMaxMonth = request.disMaxMonth,
                              disPrice = request.disPrice, effectDate = request.effectDate, expireDate = request.expireDate,
                              delYn = YN.N))?.let { discountClass ->
                return CommonResult.data(discountClass)
            }?: kotlin.run {
                return CommonResult.error("Admin createDiscountTicket failed")
            }
        }catch (e: CustomException){
            logger.error { "Admin createDiscountTicket failed $e" }
            return CommonResult.error("Admin createDiscountTicket failed $e")
        }
    }

    fun deleteDiscountTicket(request: Long) : CommonResult {
        return try {
            discountService.deleteDiscountClass(request).let {
                CommonResult.data(it)
            }
        }catch (e: CustomException){
            logger.error { "Admin deleteDiscountTicket failed $e" }
            CommonResult.error("Admin deleteDiscountTicket failed $e")
        }
    }

    fun getTicketList() : CommonResult {
        return CommonResult.data(productService.getTicketClass())
    }

    fun createTicketClass(request: TicketClass): CommonResult {
        try {
            productService.createTicketClass(request)?.let {
                return CommonResult.data(it)
            }?: kotlin.run {
                return CommonResult.error("Admin create ticket-class failed")
            }
        } catch (e: CustomException){
            return CommonResult.error("Admin create ticket-class failed")
        }
    }
}

fun singleTimer(delay: Long = 1000, unit: TimeUnit = TimeUnit.MILLISECONDS) =
    Single.timer(delay, unit)

fun interval(period: Long = 10000, unit: TimeUnit = TimeUnit.MILLISECONDS, initDelay: Long = -1) =
    if (initDelay == -1L)
        Observable.interval(period, unit)
    else
        Observable.interval(initDelay, period, unit)
