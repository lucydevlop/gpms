package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.io.glnt.gpms.model.dto.RequestParkInDTO
import io.glnt.gpms.model.enums.OpenActionType
import io.glnt.gpms.service.GateService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class RelayResource (
    private val parkinglotService: ParkinglotService,
    private val gateService: GateService
){
    companion object : KLogging()

    fun parkIn(@Valid @RequestBody requestParkInDTO: RequestParkInDTO): ResponseEntity<CommonResult> {
        logger.warn {" ##### 입차 요청 START #####"}
        logger.warn {" 차량번호 ${requestParkInDTO.vehicleNo} LPR시설정보 ${requestParkInDTO.dtFacilitiesId} 입차시간 ${requestParkInDTO.date} UUID ${requestParkInDTO.uuid} OCR결과 ${requestParkInDTO.resultcode}"  }

        parkinglotService.getGateInfoByDtFacilityId(requestParkInDTO.dtFacilitiesId ?: "")?.let { gate ->
            // 후방 카메라 입차 시 시설 연계 OFF 로 변경.
            // 단, gate 오픈 설정이 none 이 아닌 경우 on 으로 설정
            var action = !requestParkInDTO.uuid.isNullOrEmpty() && gate.openAction == OpenActionType.NONE


            return CommonResult.returnResult(CommonResult.data())
        }?: kotlin.run {
            logger.warn {" ##### 입차 요청 ERROR ${requestParkInDTO.dtFacilitiesId} gate not found #####"}
            throw CustomException(
                "${requestParkInDTO.dtFacilitiesId} gate not found",
                ResultCode.FAILED
            )
        }
    }

}