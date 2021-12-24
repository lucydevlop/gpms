package io.glnt.gpms.common.api

import io.glnt.gpms.common.utils.RestAPIManagerUtil
import io.glnt.gpms.handler.inout.model.reqAddParkIn
import io.glnt.gpms.model.dto.external.ReqEnterNotiDTO
import io.glnt.gpms.model.entity.Gate
import mu.KLogging
import org.springframework.stereotype.Component
import java.lang.RuntimeException

@Component
class ExternalClient(
    private var restAPIManager: RestAPIManagerUtil
) {
    companion object : KLogging()

    // 입차 통보 외부 연계
    fun sendEnterNoti(requestParkInDTO: reqAddParkIn, gate: Gate, url: String) {
        try {
            restAPIManager.sendPostRequest(
                "$url/parkinglot/paystation",
                ReqEnterNotiDTO(
                    vehicleNo = requestParkInDTO.vehicleNo,
                    inDate = requestParkInDTO.date,
                    gateId = gate.gateId,
                    gateName = gate.gateName
                )
            )
        } catch (e: RuntimeException) {
            logger.error { "입차 통보 연계 에러 $url ${requestParkInDTO.vehicleNo} ${requestParkInDTO.date} ${gate.gateId} ${gate.gateName}" }
        }
    }
}