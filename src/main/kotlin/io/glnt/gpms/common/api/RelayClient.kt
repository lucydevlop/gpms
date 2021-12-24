package io.glnt.gpms.common.api

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqPaystation
import io.glnt.gpms.handler.facility.model.reqSendDisplay
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.model.dto.entity.FacilityDTO
import io.glnt.gpms.model.dto.entity.GateDTO
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.LprTypeStatus
import io.glnt.gpms.model.mapper.FacilityMapper
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.service.GateService
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class RelayClient (
    private var restAPIManager: RestAPIManagerUtil,
    private var gateService: GateService,
    private var facilityRepository: ParkFacilityRepository,
    private var facilityMapper: FacilityMapper,
    private var parkSiteInfoService: ParkSiteInfoService
) {
    companion object : KLogging()

    lateinit var gateDTOs : List<GateDTO>
    lateinit var facilityDTOs: List<FacilityDTO>

    @PostConstruct
    fun postConstruct() {
        gateDTOs = gateService.findAll().filter { g -> g.delYn == YN.N }
        facilityDTOs = facilityRepository.findAll().map(facilityMapper::toDTO).filter { f -> f.delYn == YN.N }
    }

    fun sendShowDisplayMessages(gateId: String, type: String?, data: ArrayList<reqDisplayMessage>, reset: String ) {
        logger.warn { "전광판 메세지 $gateId $type $data $reset" }
        getFacilityByGateId(gateId, FacilityCategoryType.DISPLAY, type)?.let { facilityDTOs ->
            facilityDTOs.forEach { facilityDTO ->
                restAPIManager.sendPostRequest(getUrl(gateId)+"/display/show",
                    reqSendDisplay(facilityDTO.dtFacilitiesId!!, data as ArrayList<reqDisplayMessage>, reset)
                )
            }
        }
    }

    fun sendActionBreaker(gateId: String, action: String, manual: String? = null) {
        logger.warn { "차단기 메세지 $gateId $action $manual " }
        getFacilityByGateId(gateId, FacilityCategoryType.BREAKER)?.let { facilityDTOs ->
            facilityDTOs.forEach { facilityDTO ->
                if (manual.isNullOrEmpty()) {
                    restAPIManager.sendGetRequest(
                        getUrl(gateId)+"/breaker/${facilityDTO.dtFacilitiesId}/$action"
                    )
                } else {
                    restAPIManager.sendGetRequest(
                        getUrl(gateId)+"/breaker/${facilityDTO.dtFacilitiesId}/$action/manual"
                    )
                }
            }
        }
    }

    fun sendPayStation(gateId: String, type: String, requestId: String, data: Any, dtFacilityId: String? = null) {
        logger.warn { "정산기 메세지 $gateId $type $requestId $data " }
        if (dtFacilityId.isNullOrEmpty()) {
            getFacilityByGateId(gateId, FacilityCategoryType.PAYSTATION)?.let { facilityDTOs ->
                facilityDTOs.forEach { facilityDTO ->
                    restAPIManager.sendPostRequest(
                        getUrl(gateId)+"/parkinglot/paystation",
                        reqPaystation(
                            dtFacilityId = facilityDTO.dtFacilitiesId ?: "",
                            data = setPaystationRequest(type, requestId, data))
                    )
                }
            }
        } else {
            restAPIManager.sendPostRequest(
                getUrl(gateId)+"/parkinglot/paystation",
                reqPaystation(
                    dtFacilityId = dtFacilityId,
                    data = setPaystationRequest(type, requestId, data))
            )
        }

    }

    private fun getGate(gateId: String) : GateDTO? {
        return gateDTOs.find { g -> g.gateId == gateId }
    }

    private fun getUrl(gateId: String) : String? {
        return getGate(gateId)?.relaySvr
    }

    private fun getFacilityByGateId(gateId: String, category: FacilityCategoryType, type: String? = null): List<FacilityDTO>? {
        getGate(gateId)?.let { gateDTO ->
            return when(gateDTO.gateType) {
                GateTypeStatus.IN_OUT -> {
                    if (!type.isNullOrEmpty()) {
                        if (type == "IN") {
                            return facilityDTOs.filter { f -> f.gateId == gateId && f.category == category && f.lprType == LprTypeStatus.INFRONT }
                        } else {
                            return facilityDTOs.filter { f -> f.gateId == gateId && f.category == category && f.lprType == LprTypeStatus.OUTFRONT }
                        }
                    }
                    return facilityDTOs.filter { f -> f.gateId == gateId && f.category == category }
                }
                else -> {
                    facilityDTOs.filter { f -> f.gateId == gateId && f.category == category }
                }
            }
        }?: kotlin.run { return null }
    }

    private fun setPaystationRequest(type: String, requestId: String?, contents: Any) : reqApiTmapCommon {
        return reqApiTmapCommon(
            type = type,
            parkingSiteId = parkSiteInfoService.getParkSiteId(),
            requestId = requestId?.let { requestId },
            eventDateTime = DateUtil.stringToNowDateTime(),
            contents = contents
        )
    }
}