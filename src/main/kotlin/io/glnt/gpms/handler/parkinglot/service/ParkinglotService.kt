package io.glnt.gpms.handler.parkinglot.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.model.reqAddParkinglotFeature
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.common.utils.FileUtils
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.parkinglot.model.reqCreateParkinglot
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.lang.RuntimeException
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
    private lateinit var parkFeatureRepository: ParkFeatureRepository

    @Autowired
    private lateinit var parkFacilityRepository: ParkFacilityRepository

    @Autowired
    private lateinit var parkSiteInfoRepository: ParkSiteInfoRepository

    @Autowired
    private lateinit var parkGateRepository: ParkGateRepository

    fun createParkinglot(): CommonResult {
        logger.debug { "createParkinglot service" }
        try {
            // todo reqTmapFacilitiesList
            val mapData : ArrayList<parkinglotMap> = ArrayList()
            val gateList : ArrayList<gateLists> = ArrayList()
            val facilitiesList: ArrayList<facilitiesLists> = ArrayList()
            val gateData = parkGateRepository.findByFlagUse(1)
            gateData.forEach { gate ->
                val facilities = parkFacilityRepository.findByGateIdAndFlagUse(gate.gateId!!, 1)!!
                val FacilitiesId = facilities.map { it.dtFacilitiesId.toString() }.toTypedArray()
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
        } catch (e: RuntimeException) {
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
        }catch (e: RuntimeException) {
            logger.error { "getParkinglot error ${e.message}" }
            return CommonResult.error("parkinglot fetch failed ")
        }
    }

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
        requet.featureId?.let {
            val list = parkFeatureRepository.findByFeatureId(it)
            return if (list == null) CommonResult.notfound("parkinglot feature") else CommonResult.data(list)
        } ?: run {
            requet.gateSvrKey?.let {
                val lists = parkFeatureRepository.findByGroupKey(it)
                return if (lists.isNullOrEmpty()) CommonResult.notfound("parkinglot feature") else CommonResult.data(lists)
            } ?: run {
                parkFeatureRepository.findAll().let {
                    return CommonResult.data(it)
                }
            }
        }
    }

    fun getParkinglotfacilities(requet: reqSearchParkinglotFeature): CommonResult {
        requet.facilitiesId?.let {
            val list = parkFacilityRepository.findByFacilitiesId(it)
            return if (list == null) CommonResult.notfound("parkinglot facilities") else CommonResult.data(list)
        } ?: run {
            requet.gateSvrKey?.let {
                val lists = parkFacilityRepository.findByGateSvrKey(it)
                return if (lists.isNullOrEmpty()) CommonResult.notfound("parkinglot facilities") else CommonResult.data(lists)
            } ?: run {
                parkFacilityRepository.findAll().let {
                    return CommonResult.data(it)
                }
            }
        }
    }

    fun fetchParkSiteInfo() {
        parkSiteInfoRepository.findTopByOrderBySiteid()?.let {
            parkSite = it
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
            fetchParkSiteInfo()
        } catch (e: RuntimeException) {
            logger.error { "save tb_parksite error ${e.message}" }
            return false
        }
        return true
    }

    fun saveGate(data: Gate): Boolean {
        try {
            parkGateRepository.save(data)
        } catch (e: RuntimeException) {
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
        }catch (e: RuntimeException) {
            logger.error { "searchFacility error ${e.message}" }
            return CommonResult.error("facility fetch failed ")
        }

    }

    @Transactional
    fun updateParkinglot(request: reqCreateParkinglot): CommonResult = with(request) {
        logger.trace { "updateParkinglot request $request" }
        try {
            parkSiteInfoRepository.findBySiteid(request.siteId)?.let { it ->
                return CommonResult.data(
                    data = parkSiteInfoRepository.save(
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
                ))
            } ?: run {
                return CommonResult.notfound("parkinglot data not found")
            }
        } catch(e: RuntimeException) {
            logger.error { "updateParkinglot error ${e.message}" }
            return CommonResult.error("parkinglot update failed ")
        }
    }

//    fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
//        val fullOutDir = File(outDir, pathName)
//        fullOutDir.mkdirs()
//        val fullOutFile = File(fullOutDir, "$filename.json")
//
//        fullOutFile.writeText(toJsonString(false))
//    }
}