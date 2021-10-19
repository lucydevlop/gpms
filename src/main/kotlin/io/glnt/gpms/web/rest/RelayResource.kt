package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.RelayClient
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JSONUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.model.BasicPrice
import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.handler.facility.model.reqPaymentResponse
import io.glnt.gpms.handler.facility.model.reqPaymentResult
import io.glnt.gpms.handler.inout.model.reqAddParkIn
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.model.reqRelayHealthCheck
import io.glnt.gpms.handler.tmap.model.reqAdjustmentRequest
import io.glnt.gpms.handler.tmap.model.reqApiTmapCommon
import io.glnt.gpms.model.dto.*
import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.service.*
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
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
    private val parkSiteInfoService: ParkSiteInfoService,
    private val relayService: RelayService,
    private val facilityService: FacilityService,
    private val relayClient: RelayClient,
    private val parkInMapper: ParkInMapper,
    private val inoutPaymentService: InoutPaymentService,
    private val inoutResource: InoutResource
){
    companion object : KLogging()

//    fun parkIn(@Valid @RequestBody requestParkInDTO: RequestParkInDTO): ResponseEntity<CommonResult> {
//        logger.warn {" ##### 입차 요청 START #####"}
//        logger.warn {" 차량번호 ${requestParkInDTO.vehicleNo} LPR시설정보 ${requestParkInDTO.dtFacilitiesId} 입차시간 ${requestParkInDTO.date} UUID ${requestParkInDTO.uuid} OCR결과 ${requestParkInDTO.resultcode}"  }
//
//        parkinglotService.getGateInfoByDtFacilityId(requestParkInDTO.dtFacilitiesId ?: "")?.let { gate ->
//            //사진 이미지 저장
//            requestParkInDTO.base64Str?.let {
//                requestParkInDTO.fileFullPath = inoutService.saveImage(it, requestParkInDTO.vehicleNo?: "", gate.udpGateid?: "")
//                requestParkInDTO.fileName = DataCheckUtil.getFileName(requestParkInDTO.fileFullPath!!)
//                requestParkInDTO.fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
//            }
//            // 후방 카메라 입차 시 시설 연계 OFF 로 변경.
//            // 단, gate 오픈 설정이 none 이 아닌 경우 on 으로 설정
//            var action = !requestParkInDTO.uuid.isNullOrEmpty() && gate.openAction == OpenActionType.NONE
//
//
//            return CommonResult.returnResult(CommonResult.data())
//        }?: kotlin.run {
//            logger.warn {" ##### 입차 요청 ERROR ${requestParkInDTO.dtFacilitiesId} gate not found #####"}
//            throw CustomException(
//                "${requestParkInDTO.dtFacilitiesId} gate not found",
//                ResultCode.FAILED
//            )
//        }
//    }

    // Health Check
    @RequestMapping(value=["/relay/health_check"], method=[RequestMethod.POST])
    fun healthCheck(@RequestBody request: reqRelayHealthCheck) {
        logger.debug { "[Health Check] category $request" }
        relayService.facilitiesHealthCheck(request)
    }

    // (사전) 정산기 번호 검색 요청
    @RequestMapping(value = ["/relay/paystation/search/vehicle/{dtFacilityId}"], method = [RequestMethod.POST])
    fun searchCarNumber(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String) {
        logger.info { "번호 검색 요청 $request " }
        relayService.searchCarNumber(request, dtFacilityId)
    }

    // 번호 검색 후 출차(결제) 요청
    @RequestMapping(value = ["/relay/paystation/request/adjustment/{dtFacilityId}"], method = [RequestMethod.POST])
    fun requestAdjustment(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String) {
        logger.info { "정산기 결제 요청 $dtFacilityId $request " }
        val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqAdjustmentRequest::class.java)
        parkinglotService.getGateInfoByDtFacilityId(dtFacilityId )?.let { gate ->
            if (gate.gateType == GateTypeStatus.ETC) {
                // 결제만 처리
                val inSn = contents.inSn ?: "-1"
                parkInService.findOne(inSn.toLong())?.let { parkInDTO ->
                    val price = if ( parkinglotService.isPaid()) {
                        inoutService.calcParkFee("OUT", parkInDTO.inDate!!, LocalDateTime.now(), VehicleType.SMALL, parkInDTO.vehicleNo ?: "", parkInDTO.sn ?: -1)
                    } else null

                    // 정산 처리
                    val parkOutDTO = ParkOutDTO(
                                        inSn = parkInDTO.sn,
                                        vehicleNo = parkInDTO.vehicleNo,
                                        payfee = price?.totalPrice ?: 0,
                                        parktime = price?.parkTime ?: DateUtil.diffMins(parkInDTO.inDate!!, LocalDateTime.now()),
                                        discountfee = price?.discountPrice ?: 0,
                                        dayDiscountfee = price?.dayilyMaxDiscount ?: 0,
                                        parkfee = price?.orgTotalPrice ?: 0
                    )
                    inoutService.waitFacilityIF("PREPAYMENT", parkInDTO.parkcartype ?: "", parkInDTO.vehicleNo!!, gate, parkOutDTO, parkInDTO.inDate!!, dtFacilityId)
                }
            } else {
                // 출차 진행
                inoutResource.parkOut(RequestParkOutDTO(
                            vehicleNo = contents.vehicleNumber,
                            dtFacilitiesId = facilityService.getOneFacilityByGateIdAndCategory(gate.gateId, FacilityCategoryType.LPR)!!.dtFacilitiesId,
                            date = LocalDateTime.now(),
                            resultcode = "0",
                            uuid = JSONUtil.generateRandomBasedUUID()
                ))
            }
        }
    }

    // 결제 완료 후 정보 전달
    @RequestMapping(value=["/relay/paystation/result/{dtFacilityId}"], method=[RequestMethod.POST])
    fun resultPayment(@RequestBody request: reqApiTmapCommon, @PathVariable dtFacilityId: String): ResponseEntity<CommonResult> {
        logger.info { "정산 완료 $dtFacilityId $request " }

        val contents = JSONUtil.readValue(JSONUtil.getJsObject(request.contents).toString(), reqPaymentResult::class.java)
        val sn = (request.requestId ?: "-1").toLong() // inoutPayment sn

        parkinglotService.getGateInfoByDtFacilityId(dtFacilityId )?.let { gate ->
            if (gate.gateType == GateTypeStatus.ETC) {
                logger.warn { "사전 정산 완료 $dtFacilityId ${contents.vehicleNumber} " }
                inoutService.savePayment(contents, sn)
            } else {
                logger.warn { "출차 정산 완료 $dtFacilityId ${contents.vehicleNumber} " }
                // 정상 출차 시
                inoutPaymentService.findOne(sn).ifPresent { inoutPayment ->
                    val parkOut = parkOutService.findByInSn(inoutPayment.inSn ?: -1).orElse(null)
                    parkOut?.let { parkOutDTO ->
                        inoutService.savePayment(contents, sn, parkOut.sn)
                        parkInService.findOne(parkOutDTO.inSn ?: -1)?.let {

                            val parkCarType = when(contents.result?: ResultType.SUCCESS) {
                                ResultType.ERROR -> "ERROR"
                                ResultType.FAILURE -> "FAILURE"
                                else -> parkOut.parkcartype ?: ""
                            }

                            inoutService.outFacilityIF(
                                parkCarType, parkOut.vehicleNo ?: "", gate, parkInMapper.toEntity(it), parkOut.sn!!)
                        }
                    }
                }
            }
            relayClient.sendPayStation(
                gateId = gate.gateId,
                type = "paymentResponse",
                requestId = request.requestId!!,
                data = reqPaymentResponse(
                    chargingId = contents.transactionId,
                    vehicleNumber = contents.vehicleNumber
                ),
                dtFacilityId
            )
        }
        return ResponseEntity.ok(CommonResult.data())
    }

}