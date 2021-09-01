package io.glnt.gpms.common.api

import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.handler.facility.model.reqDisplayMessage
import io.glnt.gpms.handler.facility.model.reqSendDisplay
import io.glnt.gpms.model.dto.FacilityDTO
import io.glnt.gpms.model.dto.GateDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.LprTypeStatus
import io.glnt.gpms.model.mapper.FacilityMapper
import io.glnt.gpms.model.repository.ParkFacilityRepository
import io.glnt.gpms.service.GateService
import mu.KLogging
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

@Component
class RelayClient (
    private var restAPIManager: RestAPIManagerUtil,
    private var gateService: GateService,
    private var facilityRepository: ParkFacilityRepository,
    private var facilityMapper: FacilityMapper
) {
    companion object : KLogging()

    lateinit var gateDTOs : List<GateDTO>
    lateinit var facilityDTOs: List<FacilityDTO>

    @PostConstruct
    fun postConstruct() {
        gateDTOs = gateService.findAll().filter { g -> g.delYn == DelYn.N }
        facilityDTOs = facilityRepository.findAll().map(facilityMapper::toDTO).filter { f -> f.delYn == DelYn.N }
    }

    fun sendShowDisplayMessages(gateId: String, type: String?, data: ArrayList<reqDisplayMessage>, reset: String ) {
        logger.trace { "전광판 메세지 $gateId $type $data $reset" }
        getFacilityByGateId(gateId, FacilityCategoryType.DISPLAY, type)?.let { facilityDTOs ->
            facilityDTOs.forEach { facilityDTO ->
                restAPIManager.sendPostRequest(getUrl(gateId)+"/display/show",
                    reqSendDisplay(facilityDTO.dtFacilitiesId!!, data as ArrayList<reqDisplayMessage>, reset)
                )
            }
        }
    }

    fun sendActionBreaker(gateId: String, action: String, manual: String? = null) {
        logger.trace { "차단기 메세지 $gateId $action $manual " }
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
}