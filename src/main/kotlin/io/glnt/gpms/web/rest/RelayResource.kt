package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.dto.ParkOutDTO
import io.glnt.gpms.model.dto.ParkinglotVehicleDTO
import io.glnt.gpms.model.dto.RequestParkInDTO
import io.glnt.gpms.model.dto.RequestParkOutDTO
import io.glnt.gpms.model.entity.ParkinglotVehicleId
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.OpenActionType
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.service.*
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class RelayResource (
    private val parkinglotService: ParkinglotService,
    private val inoutService: InoutService,
    private val parkInService: ParkInService,
    private val parkOutService: ParkOutService,
    private val parkinglotVehicleService: ParkinglotVehicleService
){
    companion object : KLogging()

    fun parkIn(@Valid @RequestBody requestParkInDTO: RequestParkInDTO): ResponseEntity<CommonResult> {
        logger.warn {" ##### 입차 요청 START #####"}
        logger.warn {" 차량번호 ${requestParkInDTO.vehicleNo} LPR시설정보 ${requestParkInDTO.dtFacilitiesId} 입차시간 ${requestParkInDTO.date} UUID ${requestParkInDTO.uuid} OCR결과 ${requestParkInDTO.resultcode}"  }

        parkinglotService.getGateInfoByDtFacilityId(requestParkInDTO.dtFacilitiesId ?: "")?.let { gate ->
            //사진 이미지 저장
            requestParkInDTO.base64Str?.let {
                requestParkInDTO.fileFullPath = inoutService.saveImage(it, requestParkInDTO.vehicleNo?: "", gate.udpGateid?: "")
                requestParkInDTO.fileName = DataCheckUtil.getFileName(requestParkInDTO.fileFullPath!!)
                requestParkInDTO.fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
            }
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

    @RequestMapping(value = ["/inout/parkout"], method = [RequestMethod.POST])
    fun parkOut(@Valid @RequestBody requestParkOutDTO: RequestParkOutDTO) : ResponseEntity<CommonResult> {
        logger.warn {" ##### 출차 요청 START #####"}
        logger.warn {" 차량번호 ${requestParkOutDTO.vehicleNo} LPR시설정보 ${requestParkOutDTO.dtFacilitiesId} 입차시간 ${requestParkOutDTO.date} UUID ${requestParkOutDTO.uuid} OCR결과 ${requestParkOutDTO.resultcode}"  }

        parkinglotService.getGateInfoByDtFacilityId(requestParkOutDTO.dtFacilitiesId ?: "")?.let { gate ->
            // 출차 skip
            // 1. uuid 동일 기존 출차 데이터가 미인식 차량이 아닌 경우
            requestParkOutDTO.uuid?.let {  uuid ->
                parkOutService.findByUuid(uuid)?.let {
                    if (it.parkcartype!!.contains("RECOGNIZED")) {
                        throw CustomException(
                            "${requestParkOutDTO.uuid} uuid is exists",
                            ResultCode.FAILED
                        )
                    }
                }
            }

            // 사진 이미지 저장
            requestParkOutDTO.base64Str?.let {
                requestParkOutDTO.fileFullPath = inoutService.saveImage(it, requestParkOutDTO.vehicleNo?: "", gate.udpGateid?: "")
                requestParkOutDTO.fileName = DataCheckUtil.getFileName(requestParkOutDTO.fileFullPath!!)
                requestParkOutDTO.fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
            }

            requestParkOutDTO.parkCarType = inoutService.confirmParkCarType(requestParkOutDTO.vehicleNo ?: "", requestParkOutDTO.date!!)
            requestParkOutDTO.recognitionResult = if (requestParkOutDTO.parkCarType == "UNRECOGNIZED") "NOTRECOGNITION" else "RECOGNITION"

            // 모든 사진 정보 저장(차후 미인식 리스트 이용)
            parkinglotVehicleService.save(
                ParkinglotVehicleDTO(
                    id = ParkinglotVehicleId(sn = null, date = requestParkOutDTO.date),
                    vehicleNo = requestParkOutDTO.vehicleNo,
                    type = GateTypeStatus.OUT,
                    uuid = requestParkOutDTO.uuid,
                    image = requestParkOutDTO.fileFullPath,
                    delYn = DelYn.N
                )
            )

            // 입차 확인
            val parkIn = if (requestParkOutDTO.parkCarType != "UNRECOGNIZED") parkInService.getLastVehicleNoByDate(requestParkOutDTO.vehicleNo?: "", requestParkOutDTO.date!!) else null

            // 기존 출차 데이터 확인
            val existsParkOut = parkIn?.sn?.let { sn ->
                parkOutService.findByInSn(sn).orElse(null)
            }?: kotlin.run { null }
            // 출차 기준 skip
            //입차 확인 후 skip
            existsParkOut?.let { parkOut ->
                parkInService.findOne(parkOut.inSn ?: -1)?.let {
                    if (it.outSn != null && it.outSn!! > 0L) {
                        throw CustomException(
                            "${it.sn} ${requestParkOutDTO.vehicleNo} 이미 출차 처리된 차량",
                            ResultCode.FAILED
                        )
                    }
                }
            }

            //todo 사전 정산 시 inout-payment 데이터 확인 후 legTime 이후 out_date 이면 시간만큼 요금 계산
            //유료 주차장인 경우 요금 계산
            var price = if ( parkIn!= null && parkinglotService.isPaid() && requestParkOutDTO.parkCarType != "UNRECOGNIZED") {
                inoutService.calcParkFee("OUT", parkIn.inDate!!, requestParkOutDTO.date!!, VehicleType.SMALL, requestParkOutDTO.vehicleNo ?: "", parkIn.sn ?: -1)
            } else null

            // parkOut 데이터 생성
            val parkOutDTO = parkOutService.save(
                ParkOutDTO(
                    sn = existsParkOut?.sn,
                    gateId = gate.gateId,
                    parkcartype = requestParkOutDTO.parkCarType,
                    vehicleNo = requestParkOutDTO.vehicleNo,
                    image = requestParkOutDTO.fileFullPath,
                    resultcode = requestParkOutDTO.resultcode?.toInt(),
                    requestid = parkinglotService.generateRequestId(),
                    fileuploadid = requestParkOutDTO.fileUploadId,
                    outDate = requestParkOutDTO.date,
                    uuid = requestParkOutDTO.uuid,
                    parktime = price?.parkTime ?: (parkIn?.let { DateUtil.diffMins(parkIn.inDate!!, requestParkOutDTO.date!!) }?: kotlin.run { 0 }),
                    parkfee = price?.orgTotalPrice ?: 0,
                    payfee = price?.totalPrice ?: 0,
                    discountfee = price?.discountPrice ?: 0,
                    dayDiscountfee = price?.dayilyMaxDiscount ?: 0,
                    date = requestParkOutDTO.date!!.toLocalDate(),
                    delYn = DelYn.N ,
                    inSn = parkIn?.sn )
            )

            // 번호 검색 요청 조건
            // 1. 유료 주차장인 경우 차량번호로 입차 데이터 미확인, 미인식 인 경우
            if (parkinglotService.isPaid() && (parkIn == null || requestParkOutDTO.parkCarType!!.contains("RECOGNIZED"))) {
                inoutService.searchNumberFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, requestParkOutDTO.recognitionResult!!, parkOutDTO.sn!!.toString())
            } else {
                if (parkinglotService.isPaid() && parkIn != null) {
                    // 정산 대기 처리
                    inoutService.waitFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!)
                }
                // total 0원, 무료 주차장 출차 처리
                if ( (!parkinglotService.isPaid()) || ( parkinglotService.isPaid() && parkOutDTO.payfee?: 0 <= 0)) {
                    // 출차 처리
                    inoutService.outFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
                }
            }
            return ResponseEntity.ok(CommonResult.data())

        }?: kotlin.run {
            logger.warn {" ##### 입차 요청 ERROR ${requestParkOutDTO.dtFacilitiesId} gate not found #####"}
            throw CustomException(
                "${requestParkOutDTO.dtFacilitiesId} gate not found",
                ResultCode.FAILED
            )
        }
    }

}