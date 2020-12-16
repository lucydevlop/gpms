package io.glnt.gpms.handler.parkinglot.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.Base64Util
import io.glnt.gpms.handler.parkinglot.model.reqSearchParkinglotFeature
import io.glnt.gpms.handler.parkinglot.model.reqAddParkinglotFeature
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.handler.vehicle.model.reqAddParkIn
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.tmap.model.*
import io.glnt.gpms.handler.tmap.service.TmapSendService
import io.glnt.gpms.common.utils.FileUtils
import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.entity.ParkSiteInfo
import io.glnt.gpms.model.entity.ParkFeature
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service
import java.io.File
import java.lang.RuntimeException
import java.time.LocalDate
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
            gateData!!.forEach { gate ->
                val facilities = parkFacilityRepository.findByGateIdAndFlagUse(gate.gateId!!, 1)!!
                val FacilitiesId = facilities.map { it.dtFacilitiesId.toString() }.toTypedArray()
                facilities.map {
                    facility -> facilitiesList.add(facilitiesLists(category = facility.category, modelId = facility.modelid, dtFacilitiesId = facility.dtFacilitiesId, facilitiesName = facility.fName))
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
            val requestId = DataCheckUtil.generateRequestId(parkSite.parkId!!)
            val fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
            // send_event
            tmapSendService.sendFacilitiesRegist(reqFacilitiesRegist(fileUploadId = fileUploadId), requestId, "/Users/lucy/project/glnt/parking/gpms/test.json")

            // todo db Update

            return CommonResult.created("parkinglot feature add success")
        } catch (e: RuntimeException) {
            logger.error("addParkinglotFeature error {} ", e.message)
            return CommonResult.error("parkinglot feature db add failed ")
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


//    fun JsonArray<*>.writeJSON(pathName: String, filename: String) {
//        val fullOutDir = File(outDir, pathName)
//        fullOutDir.mkdirs()
//        val fullOutFile = File(fullOutDir, "$filename.json")
//
//        fullOutFile.writeText(toJsonString(false))
//    }
}