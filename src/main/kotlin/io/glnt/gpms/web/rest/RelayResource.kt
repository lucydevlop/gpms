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
    private val parkinglotVehicleService: ParkinglotVehicleService,
    private val parkSiteInfoService: ParkSiteInfoService,
    private val relayService: RelayService,
    private val facilityService: FacilityService,
    private val relayClient: RelayClient,
    private val parkInMapper: ParkInMapper,
    private val inoutPaymentService: InoutPaymentService,
    private val fareRefService: FareRefService
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

    @RequestMapping(value = ["/inout/parkin"], method = [RequestMethod.POST])
    fun parkIn(@Valid @RequestBody requestParkInDTO: reqAddParkIn) : ResponseEntity<CommonResult> {
        logger.warn {" ##### 입차 요청 START #####"}
        logger.warn {" 차량번호 ${requestParkInDTO.vehicleNo} LPR시설정보 ${requestParkInDTO.dtFacilitiesId} 입차시간 ${requestParkInDTO.date} UUID ${requestParkInDTO.uuid} OCR결과 ${requestParkInDTO.resultcode}"  }
        // 주차장 운영일 확인 후 입차 진행
        if (!parkSiteInfoService.checkOperationDay(requestParkInDTO.date)) {
            logger.warn {" ##### 주차장 운영일이 아님 입차 처리 skip #####"}
            return ResponseEntity.ok(CommonResult.data())
        }

        val result = inoutService.parkIn(requestParkInDTO)
        return when(result.code){
            ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
            ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
            else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
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

            val parkCarType = inoutService.confirmParkCarType(requestParkOutDTO.vehicleNo ?: "", requestParkOutDTO.date!!, "OUT")
            requestParkOutDTO.parkCarType = parkCarType["parkCarType"] as String?
            requestParkOutDTO.recognitionResult = if (requestParkOutDTO.parkCarType == "UNRECOGNIZED") "NOTRECOGNITION" else "RECOGNITION"

            // 모든 사진 정보 저장(차후 미인식 리스트 이용)
            parkinglotVehicleService.save(
                ParkinglotVehicleDTO(
                    sn = null, date = requestParkOutDTO.date,
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

            var price: BasicPrice? = null
            var prePrice: BasicPrice? = null
            //유료 주차장인 경우 요금 계산
            if (parkIn!= null && parkinglotService.isPaid() && requestParkOutDTO.parkCarType != "UNRECOGNIZED") {
                // 사전 정산 시 inout-payment 데이터 확인 후 legTime 이후 out_date 이면 시간만큼 요금 계산
                val prePayments = inoutPaymentService.findByInSn(parkIn.sn ?: -1)

                if (prePayments.isNullOrEmpty()) {
                    price = inoutService.calcParkFee("OUT", parkIn.inDate!!, requestParkOutDTO.date!!, VehicleType.SMALL, requestParkOutDTO.vehicleNo ?: "", parkIn.sn ?: -1)
                } else {
                    prePayments.sortedByDescending { it.approveDateTime }
                    fareRefService.getFareBasic()?.let { cgBasic ->
                        prePrice = BasicPrice(orgTotalPrice = prePayments[0].parkFee,
                            parkTime = prePayments[0].parkTime ?: 0,
                            totalPrice = prePayments[0].amount ?: 0,
                            discountPrice = prePayments[0].discount ?: 0,
                            dayilyMaxDiscount = prePayments[0].dayDiscount ?: 0)

                        val diffMins = DateUtil.diffMins(DateUtil.stringToLocalDateTime(prePayments[0].approveDateTime!!), requestParkOutDTO.date ?: LocalDateTime.now())
                        if (diffMins > cgBasic.regTime ?: 0) {
                            price = inoutService.calcParkFee("OUT", DateUtil.stringToLocalDateTime(prePayments[0].approveDateTime!!), requestParkOutDTO.date!!, VehicleType.SMALL, requestParkOutDTO.vehicleNo ?: "", parkIn.sn ?: -1)
                        }
                    }
                }
            }

            // parkOut 데이터 생성
            val parkOutDTO = parkOutService.save(
                ParkOutDTO(
                    sn = existsParkOut?.sn,
                    gateId = gate.gateId,
                    parkcartype = requestParkOutDTO.parkCarType,
                    vehicleNo = requestParkOutDTO.vehicleNo,
                    image = requestParkOutDTO.fileFullPath,
                    resultcode = requestParkOutDTO.resultcode?.toInt(),
                    requestid = parkSiteInfoService.generateRequestId(),
                    fileuploadid = requestParkOutDTO.fileUploadId,
                    outDate = requestParkOutDTO.date,
                    uuid = requestParkOutDTO.uuid,
                    parktime = price?.parkTime?: 0.plus(prePrice?.parkTime?: 0) ?: (parkIn?.let { DateUtil.diffMins(parkIn.inDate!!, requestParkOutDTO.date!!) }?: kotlin.run { 0 }),
                    parkfee = price?.orgTotalPrice?: 0.plus(prePrice?.orgTotalPrice?: 0) ?: 0,
                    payfee = price?.totalPrice?: 0.plus(prePrice?.totalPrice?: 0) ?: 0,
                    discountfee = price?.discountPrice?: 0.plus(prePrice?.discountPrice?: 0) ?: 0,
                    dayDiscountfee = price?.dayilyMaxDiscount?: 0.plus(prePrice?.dayilyMaxDiscount?: 0) ?: 0,
                    date = requestParkOutDTO.date!!.toLocalDate(),
                    delYn = DelYn.N ,
                    inSn = parkIn?.sn,
                    originDiscountFee = price?.discountPrice?: 0,
                    originParkFee = price?.orgTotalPrice?: 0,
                    originPayFee = price?.totalPrice?: 0,
                    originDayDiscountFee = price?.dayilyMaxDiscount?: 0,
                    originParkTime = price?.parkTime?: 0
                )
            )

            // 번호 검색 요청 조건
            // 1. 유료 주차장인 경우 차량번호로 입차 데이터 미확인, 미인식 인 경우
            if (parkinglotService.isPaid() && (parkIn == null || requestParkOutDTO.parkCarType!!.contains("RECOGNIZED"))) {
                inoutService.searchNumberFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, requestParkOutDTO.recognitionResult!!, parkOutDTO.sn!!.toString())
            } else {
                if (parkinglotService.isPaid() && parkIn != null) {
                    // 정산 대기 처리
                    inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!)
                }
                // total 0원, 무료 주차장 출차 처리
                if ( (!parkinglotService.isPaid()) || ( parkinglotService.isPaid() && price?.totalPrice?: 0 > 0)) {
                    // 출차 처리
                    inoutService.outFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
                }
            }
            return ResponseEntity.ok(CommonResult.data())

        }?: kotlin.run {
            logger.warn {" ##### 출차 요청 ERROR ${requestParkOutDTO.dtFacilitiesId} gate not found #####"}
            throw CustomException(
                "${requestParkOutDTO.dtFacilitiesId} gate not found",
                ResultCode.FAILED
            )
        }
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
                parkOut(RequestParkOutDTO(
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
                        val paymentDTO = inoutService.savePayment(contents, sn, parkOut.sn)
                        parkInService.findOne(parkOutDTO.inSn ?: -1)?.let {
                            inoutService.outFacilityIF(
                                parkOut.parkcartype ?: "", parkOut.vehicleNo ?: "", gate, parkInMapper.toEntity(it), parkOut.sn!!)
                        }
                    }
                }
//                relayService.resultPayment(request.requestId!!, contents, dtFacilityId)
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