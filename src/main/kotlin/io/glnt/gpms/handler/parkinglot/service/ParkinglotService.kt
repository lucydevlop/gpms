package io.glnt.gpms.handler.parkinglot.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.common.utils.FileUtils
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.facility.model.resRelaySvrFacility
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.model.reqCreateParkinglot
import io.glnt.gpms.handler.parkinglot.model.reqUpdateGates
import io.glnt.gpms.handler.relay.service.RelayService
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.OnOff
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.annotation.PostConstruct
import javax.transaction.Transactional
import kotlin.collections.ArrayList

@Service
class ParkinglotService {
    companion object : KLogging()

    lateinit var parkSite: ParkSiteInfo

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

    @PostConstruct
    fun initalizeData() {
        parkSiteInfoRepository.findTopByOrderBySiteid()?.let {
            parkSite = it
        }
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
                val facilities = parkFacilityRepository.findByGateIdAndFlagUse(gate.gateId, 1)!!
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
            logger.error("createParkinglot error {} ", e.message)
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
            logger.error { "getParkinglot error ${e.message}" }
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
                parkGateRepository.findByDelYn(DelYn.N).let {
                    return CommonResult.data(it)
                }
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
                            checkTime = if (facility.category == "BREAKER") relayService.parkAlarmSetting.gateLimitTime else 0
                        ))
                }

            }
            return CommonResult.notfound("parkinglot facilities")
        } ?: run {
            val result = ArrayList<resRelaySvrFacility>()
            requet.relaySvrKey?.let {
                parkGateRepository.findByRelaySvrKey(it).let { gates ->
                    gates.forEach { gate ->
                        parkFacilityRepository.findByGateIdAndFlagUse(gate.gateId, 0)?.let { facilities ->
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
                                        checkTime = if (facility.category == "BREAKER") relayService.parkAlarmSetting.gateLimitTime else 0
                                    ))
                            }
                        }
                    }
                }
            } ?: run {
                parkGateRepository.findAll().let { gates ->
                    gates.forEach { gate ->
                        parkFacilityRepository.findByGateIdAndFlagUse(gate.gateId, 0)?.let { facilities ->
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
                                        checkTime = if (facility.category == "BREAKER") relayService.parkAlarmSetting.gateLimitTime else 0
                                    ))
                            }
                        }
                    }
                }
            }
            return if (result.isNullOrEmpty()) CommonResult.notfound("parkinglot facilities") else CommonResult.data(result)
        }
    }

    fun parkSiteId() : String? {
        return parkSite.parkId
    }

    fun getFacility(facilityId: String) : Facility? {
        return parkFacilityRepository.findByFacilitiesId(facilityId) ?: run {
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
        logger.trace { "updateParkinglot request $request" }
        try {
            parkSiteInfoRepository.findBySiteid(request.siteId)?.let { it ->
                val data = parkSiteInfoRepository.save(
                    ParkSiteInfo(
                        siteid = it.siteid,
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
                        parkId = parkId
                    )
                )
                initalizeData()
                return CommonResult.data(data)
            } ?: run {
                return CommonResult.notfound("parkinglot data not found")
            }
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
        return parkSite.tmapSend!! == OnOff.ON
    }

//    fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
//        val fullOutDir = File(outDir, pathName)
//        fullOutDir.mkdirs()
//        val fullOutFile = File(fullOutDir, "$filename.json")
//
//        fullOutFile.writeText(toJsonString(false))
//    }
}