package io.glnt.gpms.handler.parkinglot.service

import com.mashape.unirest.http.HttpResponse
import com.mashape.unirest.http.JsonNode
import com.mashape.unirest.http.Unirest
import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.common.utils.FileUtils
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.model.resRelaySvrFacility
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.inout.model.reqVisitorExternal
import io.glnt.gpms.handler.parkinglot.model.reqCreateParkinglot
import io.glnt.gpms.handler.parkinglot.model.reqUpdateGates
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Service
class ParkinglotService {
    companion object : KLogging()

    var parkSite: ParkSiteInfo? = null

    @Value("\${visitor-external.url}")
    var visitorExternalUrl: String? = null

    @Value("\${visitor-external.token}")
    var visitorExternalToken: String? = null

    @Autowired
    lateinit var enviroment: Environment

    @Autowired
    lateinit var tmapSendService: TmapSendService

    @Autowired
    lateinit var relayService: RelayService

    @Autowired
    lateinit var facilityService: FacilityService

    @Autowired
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkSiteInfoRepository: ParkSiteInfoRepository

    @Autowired
    private lateinit var parkGateRepository: ParkGateRepository

    @Autowired
    private lateinit var discountClassRepository: DiscountClassRepository

    @Autowired
    private lateinit var restAPIManagerUtil: RestAPIManagerUtil

    @PostConstruct
    fun initalizeData() {
        parkSiteInfoRepository.findTopByOrderBySiteid()?.let {
            parkSite = it
        }
        parkGateRepository.findByGateId("GATE001")?: run {
            parkGateRepository.saveAndFlush(
                Gate(sn = null, gateId = "GATE001", gateName = "입구게이트1", gateType = GateTypeStatus.IN, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                    seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000001", delYn = DelYn.N, resetSvr = "http://192.168.20.211/io.cgi?relay="))
        }
        parkGateRepository.findByGateId("GATE002")?: run {
            parkGateRepository.saveAndFlush(
                Gate(sn = null, gateId = "GATE002", gateName = "출구게이트1", gateType = GateTypeStatus.OUT, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                    seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000002", delYn = DelYn.N, resetSvr = "http://192.168.20.211/io.cgi?relay="))
        }
        parkGateRepository.findByGateId("GATE003")?: run {
            parkGateRepository.saveAndFlush(
                Gate(sn = null, gateId = "GATE003", gateName = "입구게이트2", gateType = GateTypeStatus.IN, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                    seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000001", delYn = DelYn.N, resetSvr = "http://192.168.20.212/io.cgi?relay="))
        }
        parkGateRepository.findByGateId("GATE004")?: run {
            parkGateRepository.saveAndFlush(
                Gate(sn = null, gateId = "GATE004", gateName = "출구게이트2", gateType = GateTypeStatus.OUT, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                    seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000002", delYn = DelYn.N, resetSvr = "http://192.168.20.212/io.cgi?relay="))
        }
    }

    fun isPaid(): Boolean {
        return parkSiteInfoRepository.findTopByOrderBySiteid()?.let {
            it.saleType == SaleType.PAID
        }?: kotlin.run { false }
    }

    fun createParkinglot(): CommonResult {
        logger.debug { "createParkinglot service" }
        try {
            // todo reqTmapFacilitiesList
            val mapData : ArrayList<parkinglotMap> = ArrayList()
            val gateList : ArrayList<gateLists> = ArrayList()
            val facilitiesList: ArrayList<facilitiesLists> = ArrayList()
            val gateData = parkGateRepository.findByDelYn(DelYn.N)
            gateData.forEach { gate ->
                val facilities = parkFacilityRepository.findByGateIdAndDelYn(gate.gateId, DelYn.Y)!!
                val FacilitiesId = facilities.map { it.dtFacilitiesId }.toTypedArray()
                facilities.map {
                    facility -> facilitiesList.add(facilitiesLists(category = facility.category, modelId = facility.modelid, dtFacilitiesId = facility.dtFacilitiesId, facilitiesName = facility.fname))
                }
                gateList.add(gateLists(
                    dtGateId = gate.gateId+"_aa",
                    gateName = gate.gateName!!,
                    gateType = gate.gateType.toString(),
                    dtFacilitiesId = FacilitiesId))
            }
            val request = reqTmapFacilitiesList(
                map = mapData, gateList = gateList, facilitiesList = facilitiesList
            )
            FileUtils.writeDatesToJsonFile(request, "/Users/lucy/project/glnt/parking/gpms/test.json")

            // tmap request 'facilitiesRegistRequest'
            val requestId = generateRequestId()
            val fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
            // send_event
            tmapSendService.sendFacilitiesRegist(reqFacilitiesRegist(fileUploadId = fileUploadId), requestId, "/Users/lucy/project/glnt/parking/gpms/test.json")

            // todo db Update

            return CommonResult.created("parkinglot feature add success")
        } catch (e: CustomException) {
            logger.error{"createParkinglot error ${e.msg} "}
            return CommonResult.error("Parkinglot db creatae failed ")
        }
    }

    fun getParkinglot() : CommonResult {
        logger.info { "getParkinglot fetch " }
        try {
            parkSiteInfoRepository.findTopByOrderBySiteid()?.let { it ->
                return CommonResult.data(it)
            } ?: run {
                return CommonResult.notfound("parkinglot site info")
            }
        }catch (e: CustomException) {
            logger.error { "getParkinglot error $e" }
            return CommonResult.error("parkinglot fetch failed ")
        }
    }

    fun getParkinglotGates(requet: reqSearchParkinglotFeature): CommonResult {
        logger.info { "getParkinglotGates request $requet" }
        try {
            requet.gateId?.let {
                val gate = parkGateRepository.findByGateId(gateId = it)
                return if (gate == null) CommonResult.notfound("gate"+requet.gateId) else CommonResult.data(gate)
            } ?: run {
//                parkGateRepository.findByDelYn(DelYn.N).let {
                return CommonResult.data(parkGateRepository.findAll())
//                }
            }
        }catch (e: CustomException) {
            logger.error("getParkinglotGates error {} ", e.message)
            return CommonResult.error("getParkinglotGates failed ")
        }
    }

    fun updateGates(request: reqUpdateGates) : CommonResult{
        logger.info { "update gates request:  $request" }
        try {
            request.gates.forEach {
                parkGateRepository.save(it)
            }
            facilityService.initalizeData()
            return CommonResult.data(parkGateRepository.findByDelYn(DelYn.N))
        } catch (e: CustomException) {
            logger.error("updateGates error {} ", e.message)
            return CommonResult.error("updateGates failed ")
        }
    }

    fun createGate(request: Gate) : CommonResult{
        logger.info { "create gate: $request" }
        try {
            return CommonResult.data(parkGateRepository.save(request))
        }catch (e: CustomException) {
            logger.error("createGate error {} ", e.message)
            return CommonResult.error("createGate failed ")
        }
    }

    fun deleteGate(id: Long) : CommonResult {
        logger.info{ "delete gate: $id"}
        try{
            parkGateRepository.findBySn(id)?.let { gate ->
                gate.delYn = DelYn.Y
                return CommonResult.data(parkGateRepository.save(gate))
            }
        }catch (e: CustomException) {
            logger.error("deleteGate error {} ", e.message)
        }
        return CommonResult.error("deleteGate failed ")
    }

    fun changeDelYnGate(gateId: String, delYn: DelYn) : CommonResult {
        logger.info { "changeDelYnGate $gateId $delYn" }
        try{
            parkGateRepository.findByGateId(gateId)?.let { gate ->
                gate.delYn = delYn
                val update = parkGateRepository.save(gate)
                parkFacilityRepository.findByGateId(update.gateId)?.let { facilities ->
                    facilities.forEach { facility ->
                        facility.delYn = delYn
                        parkFacilityRepository.save(facility)
                    }
                }
                return CommonResult.data(update)
            }
        }catch (e: CustomException) {
            logger.error("changeDelYnGate error {} ", e.message)
        }
        return CommonResult.error("changeDelYnGate failed ")
    }

    fun changeDelYnFacility(dtFacilitiesId: String, delYn: DelYn) : CommonResult {
        logger.info { "changeDelYnFacility $dtFacilitiesId $delYn" }
        try{
            getFacilityByDtFacilityId(dtFacilitiesId)?.let { facility ->
                facility.delYn = delYn
                return CommonResult.data(parkFacilityRepository.save(facility))
            }
        }catch (e: CustomException) {
            logger.error("changeDelYnGate error {} ", e.message)
        }
        return CommonResult.error("changeDelYnGate failed ")
    }

    fun getParkinglotfacilities(requet: reqSearchParkinglotFeature): CommonResult {
        requet.facilitiesId?.let {
            parkFacilityRepository.findByFacilitiesId(it)?.let { facility ->
                parkGateRepository.findByGateId(facility.gateId)?.let { gate ->
                    return CommonResult.data(
                        resRelaySvrFacility(sn = facility.sn,
                            category = facility.category, modelid = facility.modelid,
                            fname = facility.fname, dtFacilitiesId = facility.dtFacilitiesId,
                            facilitiesId = facility.facilitiesId, flagUse = facility.flagUse,
                            gateId = facility.gateId, udpGateid = facility.udpGateid,
                            ip = facility.ip, port = facility.port, sortCount = facility.sortCount,
                            resetPort = facility.resetPort, flagConnect = facility.flagConnect, lprType = facility.lprType,
                            imagePath = facility.imagePath, gateType = gate.gateType, relaySvrKey = gate.relaySvrKey,
                            checkTime = if (facility.category == "BREAKER") relayService.parkAlarmSetting.gateLimitTime else 0,
                            delYn = facility.delYn
                        ))
                }

            }
            return CommonResult.notfound("parkinglot facilities")
        } ?: run {
            val result = ArrayList<resRelaySvrFacility>()
            requet.relaySvrKey?.let {
                parkGateRepository.findByRelaySvrKey(it).let { gates ->
                    gates.forEach { gate ->
                        parkFacilityRepository.findByGateIdAndDelYn(gate.gateId, DelYn.N)?.let { facilities ->
                            facilities.forEach { facility ->
                                result.add(
                                    resRelaySvrFacility(sn = facility.sn,
                                        category = facility.category, modelid = facility.modelid,
                                        fname = facility.fname, dtFacilitiesId = facility.dtFacilitiesId,
                                        facilitiesId = facility.facilitiesId, flagUse = facility.flagUse,
                                        gateId = facility.gateId, udpGateid = facility.udpGateid,
                                        ip = facility.ip, port = facility.port, sortCount = facility.sortCount,
                                        resetPort = facility.resetPort, flagConnect = facility.flagConnect, lprType = facility.lprType,
                                        imagePath = facility.imagePath, gateType = gate.gateType, relaySvrKey = gate.relaySvrKey,
                                        checkTime = if (facility.category == "BREAKER") relayService.parkAlarmSetting.gateLimitTime else 0,
                                        delYn = facility.delYn
                                    ))
                            }
                        }
                    }
                }
            } ?: run {
                parkGateRepository.findAll().let { gates ->
                    gates.forEach { gate ->
                        parkFacilityRepository.findByGateId(gate.gateId)?.let { facilities ->
                            facilities.forEach { facility ->
                                result.add(
                                    resRelaySvrFacility(sn = facility.sn,
                                        category = facility.category, modelid = facility.modelid,
                                        fname = facility.fname, dtFacilitiesId = facility.dtFacilitiesId,
                                        facilitiesId = facility.facilitiesId, flagUse = facility.flagUse,
                                        gateId = facility.gateId, udpGateid = facility.udpGateid,
                                        ip = facility.ip, port = facility.port, sortCount = facility.sortCount,
                                        resetPort = facility.resetPort, flagConnect = facility.flagConnect, lprType = facility.lprType,
                                        imagePath = facility.imagePath, gateType = gate.gateType, relaySvrKey = gate.relaySvrKey,
                                        checkTime = if (facility.category == "BREAKER") relayService.parkAlarmSetting.gateLimitTime else 0,
                                        delYn = facility.delYn
                                    ))
                            }
                        }
                    }
                }
            }
//            return if (result.isNullOrEmpty()) CommonResult.notfound("parkinglot facilities") else CommonResult.data(result)
            return CommonResult.data(result)
        }
    }

    fun parkSiteId() : String? {
        return parkSite!!.parkId
    }

    fun parkSiteSiteId(): String? {
        return parkSite!!.siteid
    }

    fun getFacility(facilityId: String) : Facility? {
        return parkFacilityRepository.findByDtFacilitiesId(facilityId) ?: run {
            null
        }
    }

    fun getGate(gateId: String) : Gate? {
        return parkGateRepository.findByGateId(gateId)?: run {
            null
        }
    }

    fun getGateInfoByFacilityId(facilityId: String) : Gate? {
        parkFacilityRepository.findByFacilitiesId(facilityId)?.let {
            parkGateRepository.findByGateId(it.gateId)?.let {
                return it
            }
        }
        return null
    }

    fun getFacilityByDtFacilityId(dtFacilityId: String) : Facility? {
        return parkFacilityRepository.findByDtFacilitiesId(dtFacilityId) ?: run {
            null
        }
    }

    fun getGateInfoByDtFacilityId(dtFacilityId: String) : Gate? {
        parkFacilityRepository.findByDtFacilitiesId(dtFacilityId)?.let {
            parkGateRepository.findByGateId(it.gateId)?.let {
                return it
            }
        }
        return null
    }

    fun getFacilityByGateAndCategory(gate: String, category: String) : List<Facility>? {
        parkFacilityRepository.findByGateIdAndCategory(gate, category)?.let {
            return it
        }
        return null
    }

    fun getFacilityByCategory(category: String) : List<Facility>? {
        parkFacilityRepository.findByCategory(category)?.let {
            return it
        }
        return null
    }

    fun getGateInfoByUdpGateId(udpGateId: String) : Gate? {
        parkGateRepository.findByUdpGateid(udpGateId)?.let {
            return it
        }
        return null
    }

    fun saveParkSiteInfo(data: ParkSiteInfo): Boolean {
        try {
            data.flagMessage = 1
            parkSiteInfoRepository.save(data)
            initalizeData()
        } catch (e: CustomException) {
            logger.error { "save tb_parksite error ${e.message}" }
            return false
        }
        return true
    }

    fun saveGate(data: Gate): Boolean {
        try {
            parkGateRepository.save(data)
        } catch (e: CustomException) {
            logger.error { "save gate error ${e.message}" }
            return false
        }
        return true
    }

    fun generateRequestId() : String {
        return DataCheckUtil.generateRequestId(parkSiteId()!!)
    }

    @Throws(CustomException::class)
    fun searchFacility(id: String) : CommonResult {
        logger.trace { "searchFacility request $id " }
        try {
            return CommonResult.data(data =
                when(id) {
                    "ALL" -> parkFacilityRepository.findAll()
                    else -> getFacility(id)
                }
            )
        }catch (e: CustomException) {
            logger.error { "searchFacility error ${e.message}" }
            return CommonResult.error("facility fetch failed ")
        }

    }

    @Transactional
    fun updateParkinglot(request: reqCreateParkinglot): CommonResult = with(request) {
        logger.info { "updateParkinglot request $request" }
        try {
            parkSiteInfoRepository.findTopByOrderBySiteid()?.let {
                request.siteId = it.siteid
            }

            val data = parkSiteInfoRepository.save(
                ParkSiteInfo(
                    siteid = siteId,
                    sitename = siteName,
                    limitqty = limitqty,
                    saupno = saupno,
                    tel = tel,
                    ceoname = ceoname,
                    postcode = postcode,
                    address = address,
                    firsttime = firsttime,
                    firstfee = firstfee,
                    returntime = returntime,
                    overtime = overtime,
                    overfee = overfee,
                    addtime = addtime,
                    dayfee = dayfee,
                    parkingSpotStatusNotiCycle = parkingSpotStatusNotiCycle,
                    facilitiesStatusNotiCycle = facilitiesStatusNotiCycle,
                    flagMessage = flagMessage,
                    businame = businame,
                    parkId = parkId,
                    vehicleDayOption =vehicleDayOption,
                    tmapSend = tmapSend,
                    saleType = saleType,
                    externalSvr = externalSvr,
                    ip = ip, city = city,
                    space = space,
                    visitorExternal = visitorExternal,
                    visitorExternalKey = visitorExternalKey
                )
            )
            initalizeData()
            return CommonResult.data(data)
//            parkSiteInfoRepository.findBySiteid(request.siteId)?.let { it ->
//                val data = parkSiteInfoRepository.save(
//                    ParkSiteInfo(
//                        siteid = it.siteid,
//                        sitename = siteName,
//                        limitqty = limitqty,
//                        saupno = saupno,
//                        tel = tel,
//                        ceoname = ceoname,
//                        postcode = postcode,
//                        address = address,
//                        firsttime = firsttime,
//                        firstfee = firstfee,
//                        returntime = returntime,
//                        overtime = overtime,
//                        overfee = overfee,
//                        addtime = addtime,
//                        dayfee = dayfee,
//                        parkingSpotStatusNotiCycle = parkingSpotStatusNotiCycle,
//                        facilitiesStatusNotiCycle = facilitiesStatusNotiCycle,
//                        flagMessage = flagMessage,
//                        businame = businame,
//                        parkId = parkId,
//                        vehicleDayOption =vehicleDayOption,
//                        tmapSend = tmapSend,
//                        saleType = saleType,
//                        externalSvr = externalSvr,
//                        ip = ip, city = city,
//                        space = space,
//                        visitorExternal = visitorExternal,
//                        visitorExternalKey = visitorExternalKey
//                    )
//                )
//                initalizeData()
//                return CommonResult.data(data)
//            } ?: run {
//                val data = parkSiteInfoRepository.save(
//                    ParkSiteInfo(
//                        siteid = siteId,
//                        sitename = siteName,
//                        limitqty = limitqty,
//                        saupno = saupno,
//                        tel = tel,
//                        ceoname = ceoname,
//                        postcode = postcode,
//                        address = address,
//                        firsttime = firsttime,
//                        firstfee = firstfee,
//                        returntime = returntime,
//                        overtime = overtime,
//                        overfee = overfee,
//                        addtime = addtime,
//                        dayfee = dayfee,
//                        parkingSpotStatusNotiCycle = parkingSpotStatusNotiCycle,
//                        facilitiesStatusNotiCycle = facilitiesStatusNotiCycle,
//                        flagMessage = flagMessage,
//                        businame = businame,
//                        parkId = parkId,
//                        vehicleDayOption =vehicleDayOption,
//                        tmapSend = tmapSend,
//                        saleType = saleType,
//                        externalSvr = externalSvr,
//                        ip = ip, city = city,
//                        space = space,
//                        visitorExternal = visitorExternal,
//                        visitorExternalKey = visitorExternalKey
//                    )
//                )
//                initalizeData()
//                return CommonResult.data(data)
        } catch(e: CustomException) {
            logger.error { "updateParkinglot error ${e.message}" }
            return CommonResult.error("parkinglot update failed ")
        }
    }

    fun getDiscountCoupon(): CommonResult {
        logger.info { "getDiscountCounpon" }
        try {
            return CommonResult.data(
                data = discountClassRepository.findByExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(LocalDateTime.now(), LocalDateTime.now(), DelYn.N)
            )
        }catch(e: CustomException) {
            logger.error { "getDiscountCounpon error ${e.message}" }
            return CommonResult.error("getDiscountCounpon failed ")
        }
    }

    fun isTmapSend(): Boolean {
        return parkSite!!.tmapSend!! == OnOff.ON
    }

    fun isExternalSend() : Boolean {
        return parkSite!!.externalSvr != ExternalSvrType.NONE
    }

    fun isVisitorExternalKeyType(): Boolean {
        return parkSite!!.visitorExternal == VisitorExternalKeyType.APTNER
    }

    fun getVisitorExternalInfo(): HashMap<String, String?>? {
        return parkSite!!.visitorExternal?.let {
             hashMapOf<String, String?>(
                "url" to visitorExternalUrl,
                "token" to visitorExternalToken,
                "key" to parkSite!!.visitorExternalKey
             )
        }?: kotlin.run {
            null
        }
    }


    fun searchVisitorExternal(visitorExternalInfo: HashMap<String,String?>,vehicleNo: String): HttpResponse<JsonNode>?{

        val key = visitorExternalInfo["key"]
        val host = visitorExternalInfo["url"]
        val token = visitorExternalInfo["token"]

        val request = host+"/visit/check?kaptCode="+key+"&carNo="+vehicleNo

        return restAPIManagerUtil.sendGetRequestWithToken(request,token)

        //todo 데이터 포멧폼 HasMap으로 만들기 (key: isVisitor value: "Y") ...

    }

    fun sendInVisitorExternal(visitorExternalInfo: HashMap<String, String?>, visitorData: reqVisitorExternal, parkingtype: String){
        try {

            visitorExternalInfo.let {

                val host = visitorExternalInfo["url"]
                val token = visitorExternalInfo["token"]
                val url = host+"/access/in"

                restAPIManagerUtil.sendPostRequestWithToken(url, token, visitorData)?.let {
                    when(it.status){
                        200 -> logger.warn { "sendInVisitorExternal success! ${it.body}" }

                        else -> logger.warn { "sendInVisitorExternal failed. ${it.body}" }
                    }
                }?: kotlin.run {
                    logger.warn { "no receive response data" }
                }
            }
        } catch (e: RuntimeException){
            logger.error { "sendInVisitorExternal error $e" }
        }
    }
}




//    fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
//        val fullOutDir = File(outDir, pathName)
//        fullOutDir.mkdirs()
//        val fullOutFile = File(fullOutDir, "$filename.json")
//
//        fullOutFile.writeText(toJsonString(false))
//    }
//}