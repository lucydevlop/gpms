package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.RelayClient
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.calc.model.BasicPrice
import io.glnt.gpms.handler.calc.service.FareRefService
import io.glnt.gpms.handler.inout.model.reqAddParkIn
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.common.api.ExternalClient
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.model.criteria.InoutPaymentCriteria
import io.glnt.gpms.model.dto.ParkOutDTO
import io.glnt.gpms.model.dto.ParkinglotVehicleDTO
import io.glnt.gpms.model.dto.RequestParkOutDTO
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.GateMapper
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.service.*
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class InoutResource (
    private val parkSiteInfoService: ParkSiteInfoService,
    private val inoutService: InoutService,
    private val parkinglotService: ParkinglotService,
    private val gateService: GateService,
    private val gateMapper: GateMapper,
    private val parkOutService: ParkOutService,
    private val parkInService: ParkInService,
    private val parkInMapper: ParkInMapper,
    private val inoutPaymentQueryService: InoutPaymentQueryService,
    private val parkinglotVehicleService: ParkinglotVehicleService,
    private val inoutPaymentService: InoutPaymentService,
    private val fareRefService: FareRefService,
    private var relayClient: RelayClient,
    private var externalClient: ExternalClient,
    private var rcsService: RcsService,
    private var seasonTicketService: TicketService
){
    companion object : KLogging()

    @RequestMapping(value=["/inout"], method = [RequestMethod.GET])
    fun getInout(@RequestParam(name = "sn", required = false) sn: Long,): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(inoutService.getInout(sn)))
    }

    @RequestMapping(value = ["/inouts/calc"], method = [RequestMethod.POST])
    fun calc(@Valid @RequestBody resParkInList: resParkInList): ResponseEntity<CommonResult> {
        logger.debug { "inout calc $resParkInList" }
        return CommonResult.returnResult(CommonResult.data(inoutService.calcInout(resParkInList)))
    }

    @RequestMapping(value = ["/inouts"], method = [RequestMethod.PUT])
    fun update(@Valid @RequestBody resParkInList: resParkInList): ResponseEntity<CommonResult> {
        logger.debug { "inout update $resParkInList" }
        return CommonResult.returnResult(CommonResult.data(inoutService.updateInout(resParkInList)))
    }

    @RequestMapping(value = ["/inouts"], method = [RequestMethod.POST])
    fun create(@Valid @RequestBody resParkInList: resParkInList): ResponseEntity<CommonResult> {
        logger.debug { "inout create $resParkInList" }
        return CommonResult.returnResult(CommonResult.data())
    }

    @RequestMapping(value = ["/inouts/transfer"], method = [RequestMethod.PUT])
    fun parkOutTransfer(@Valid @RequestBody resParkInList: resParkInList): ResponseEntity<CommonResult> {
        logger.debug { "inout transfer $resParkInList" }
        val result = inoutService.updateInout(resParkInList)
        val gateDTO = gateService.findOne(resParkInList.outGateId ?: "").orElse(null)
        if ( parkinglotService.isPaid() ) {
            // 출차 처리
            parkOutService.findOne(resParkInList.parkoutSn!!).ifPresent { parkOutDTO ->
                gateDTO?.let { gate ->
                    inoutService.waitFacilityIF(
                        "MANPAYMENT",
                        resParkInList.parkcartype,
                        resParkInList.vehicleNo!!,
                        gateMapper.toEntity(gate)!!,
                        parkOutDTO,
                        resParkInList.inDate
                    )

                    // total 0원, 무료 주차장 출차 처리
                    if ( (!parkinglotService.isPaid()) || ( parkinglotService.isPaid() && parkOutDTO.payfee?: 0 <= 0)) {
                        // 출차 처리
                        inoutService.outFacilityIF(
                            resParkInList.parkcartype,
                            resParkInList.vehicleNo!!,
                            gateMapper.toEntity(gate)!!,
                            parkInService.findOne(resParkInList.parkinSn!!)?.let { parkInMapper.toEntity(it) },
                            parkOutDTO.sn!!)
                    }
                }
            }
        }
        return CommonResult.returnResult(CommonResult.data(result))
    }

    @RequestMapping(value = ["/inouts/payments"], method = [RequestMethod.GET])
    fun getInoutPayments(@RequestParam(name = "fromDate", required = false) fromDate: String,
                         @RequestParam(name = "toDate", required = false) toDate: String,
                         @RequestParam(name = "vehicleNo", required = false) vehicleNo: String): ResponseEntity<CommonResult> {
        val result = inoutPaymentQueryService.findByCriteria(InoutPaymentCriteria(
                                                                fromDate = DateUtil.stringToLocalDate(fromDate),
                                                                toDate = DateUtil.stringToLocalDate(toDate),
                                                                vehicleNo = vehicleNo))
        return CommonResult.returnResult(
//            CommonResult.data(result.filter { it -> it.result != ResultType.WAIT })
            CommonResult.data(result.filter { it -> (it.amount?: 0) > 0 })
        )
    }

    @RequestMapping(value=["/inouts/forced/exit/{sn}"], method = [RequestMethod.DELETE])
    fun forcedExit(@PathVariable sn: Long): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.forcedExit(sn))
    }

    @RequestMapping(value = ["/inout/parkin"], method = [RequestMethod.POST])
    fun parkIn(@Valid @RequestBody requestParkInDTO: reqAddParkIn) : ResponseEntity<CommonResult> {
        logger.warn {" ##### 입차 요청 START #####"}
        logger.warn {" 차량번호 ${requestParkInDTO.vehicleNo} LPR시설정보 ${requestParkInDTO.dtFacilitiesId} 입차시간 ${requestParkInDTO.date} UUID ${requestParkInDTO.uuid} OCR결과 ${requestParkInDTO.resultcode}"  }
        // 긴급 차량 확인
        requestParkInDTO.isEmergency = DataCheckUtil.isEmergency(requestParkInDTO.vehicleNo)
        // 차량번호 패턴 체크
        if (DataCheckUtil.isValidCarNumber(requestParkInDTO.vehicleNo)) {
            requestParkInDTO.parkingtype = "NORMAL"
            requestParkInDTO.recognitionResult = "RECOGNITION"
        } else {
            requestParkInDTO.parkingtype = "UNRECOGNIZED"
            requestParkInDTO.recognitionResult = "NOTRECOGNITION"
        }

        parkinglotService.getGateInfoByDtFacilityId(requestParkInDTO.dtFacilitiesId ?: "")?.let { gate ->
            if (!requestParkInDTO.isEmergency!!) {
                // 입차 skip
                // 1. 주차장 운영일 확인
                if (!parkSiteInfoService.checkOperationDay(requestParkInDTO.date)) {
                    logger.warn {" ##### 주차장 운영일이 아님 입차 처리 skip #####"}
                    inoutService.inFacilityIF("RESTRICTE", requestParkInDTO.vehicleNo, gate.gateId, gate.openAction?: OpenActionType.NONE)
                    return ResponseEntity.ok(CommonResult.data())
                }
                // 2. 차량 요일제 확인
                parkSiteInfoService.parkSite!!.vehicleDayOption?.let {
                    if (requestParkInDTO.recognitionResult == "RECOGNITION" && it != VehicleDayType.OFF) {
                        if (DataCheckUtil.isRotation(it, requestParkInDTO.vehicleNo)) {
                        } else {
                            logger.warn {" ##### 입차 차단 요일제적용 차량번호 ${requestParkInDTO.vehicleNo} #####"}
                            inoutService.inFacilityIF("RESTRICTE", requestParkInDTO.vehicleNo, gate.gateId, gate.openAction?: OpenActionType.NONE)
                            return ResponseEntity.ok(CommonResult.data("Restricte vehicle $requestParkInDTO.vehicleNo $requestParkInDTO.parkingtype"))
                        }
                    }
                }
                // 3. 만차 제어
                if (parkSiteInfoService.parkSite!!.space != null) {
                    parkSiteInfoService.parkSite!!.space?.let { spaces ->
                        logger.info("parkinglot space $spaces")
                        gate.gateGroupId?.let { groupId ->
                            if (groupId.equals(spaces["gateGroupId"].toString())) {
                                gateService.findGateByGateGroupId(groupId).let { items ->
                                    val inGates = ArrayList<String>()
                                    for (item in items) {
                                        inGates.add(item.gateId?: "")
                                    }
                                    if (inoutService.countParkInByGatesAndVehicleStatus(inGates, "IN") >= spaces["space"].toString().toInt()) {
                                        logger.warn{"##### 입차 차단 만차적용 차량번호: ${requestParkInDTO.vehicleNo} ${spaces["space"].toString().toInt()} #####"}
                                        inoutService.inFacilityIF("FULL", requestParkInDTO.vehicleNo, gate.gateId, gate.openAction?: OpenActionType.NONE)
                                        return ResponseEntity.ok(CommonResult.data("Full limit $requestParkInDTO.vehicleNo $requestParkInDTO.parkingtype"))
                                    }
                                }

                            }
                        }
                    }
                }
            }

            // 이미지 사진 저장
            requestParkInDTO.base64Str?.let {
                requestParkInDTO.fileFullPath = inoutService.saveImage(it, requestParkInDTO.vehicleNo?: "", gate.udpGateid?: "")
                requestParkInDTO.fileName = DataCheckUtil.getFileName(requestParkInDTO.fileFullPath!!)
                requestParkInDTO.fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
            }

            // 모든 사진 정보 저장(차후 미인식 리스트 이용)
            parkinglotVehicleService.save(
                ParkinglotVehicleDTO(
                    sn = null, date = requestParkInDTO.date,
                    vehicleNo = requestParkInDTO.vehicleNo,
                    type = GateTypeStatus.IN,
                    uuid = requestParkInDTO.uuid,
                    image = requestParkInDTO.fileFullPath,
                    delYn = DelYn.N
                )
            )

            val result = inoutService.parkIn(requestParkInDTO)

            // 입차 통보 여부 확인
            parkSiteInfoService.getEnterNoti()?.let { enterNotiDTO ->
                enterNotiDTO.use?.let { use ->
                    if (use == OnOff.ON) {
                        externalClient.sendEnterNoti(requestParkInDTO, gate, enterNotiDTO.url?: "")
                    }
                }
            }

            return when(result.code){
                ResultCode.CREATED.getCode() -> ResponseEntity(result, HttpStatus.CREATED)
                ResultCode.SUCCESS.getCode() -> ResponseEntity(result, HttpStatus.OK)
                else -> ResponseEntity(result, HttpStatus.BAD_REQUEST)
            }
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
            }?: kotlin.run {
                // One way 등 후진등으로 인하여 재출차 요청 시
                if (requestParkOutDTO.recognitionResult == "RECOGNITION") {
                    parkOutService.findByLastVehicleNo(requestParkOutDTO.vehicleNo ?: "", gate.gateId).orElse(null)?.let { parkOutDTO ->
                        if (DateUtil.diffMins(parkOutDTO.outDate ?: LocalDateTime.now(), requestParkOutDTO.date?: LocalDateTime.now()) < 3) {
                            logger.warn { "출차 후 재출차 3분 미만 차단기 open 처리 ${requestParkOutDTO.vehicleNo} 기존 출차 시간 ${parkOutDTO.outDate} 현재 출차 시간 ${requestParkOutDTO.date}" }
                            relayClient.sendActionBreaker(gate.gateId, "open")
                            return ResponseEntity.ok(CommonResult.data())
                        }
                    }
                }
                null
            }
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
                val prePayments = inoutPaymentService.findByInSnAndResult(parkIn.sn ?: -1, ResultType.SUCCESS)

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
                        if (diffMins > (cgBasic.regTime ?: 0)) {
                            price = inoutService.calcParkFee("OUT", DateUtil.stringToLocalDateTime(prePayments[0].approveDateTime!!), requestParkOutDTO.date!!, VehicleType.SMALL, requestParkOutDTO.vehicleNo ?: "", parkIn.sn ?: -1)
                        }
                    }
                }
            }

            var parkOutDTO = ParkOutDTO(sn = existsParkOut?.sn,
                gateId = gate.gateId,
                parkcartype = requestParkOutDTO.parkCarType,
                vehicleNo = requestParkOutDTO.vehicleNo,
                image = requestParkOutDTO.fileFullPath,
                resultcode = requestParkOutDTO.resultcode?.toInt(),
                requestid = parkSiteInfoService.generateRequestId(),
                fileuploadid = requestParkOutDTO.fileUploadId,
                outDate = requestParkOutDTO.date,
                uuid = requestParkOutDTO.uuid,
                parktime = (price?.parkTime ?: 0) + (prePrice?.parkTime ?: 0),
                parkfee = (price?.orgTotalPrice ?: 0) + (prePrice?.orgTotalPrice ?: 0),
                payfee = (price?.totalPrice ?: 0) + (prePrice?.totalPrice ?: 0),
                discountfee = (price?.discountPrice ?: 0) + (prePrice?.discountPrice ?: 0),
                dayDiscountfee = (price?.dayilyMaxDiscount ?: 0) + (prePrice?.dayilyMaxDiscount ?: 0),
                date = requestParkOutDTO.date!!.toLocalDate(),
                delYn = DelYn.N ,
                inSn = parkIn?.sn,
                originDiscountFee = price?.discountPrice?: 0,
                originParkFee = price?.orgTotalPrice?: 0,
                originPayFee = price?.totalPrice?: 0,
                originDayDiscountFee = price?.dayilyMaxDiscount?: 0,
                originParkTime = price?.parkTime?: 0 )

            // parkOut 데이터 생성
            parkOutDTO = parkOutService.save(parkOutDTO)

            // 무료 주차장인 경우
            if (!parkinglotService.isPaid()) {
                // 출차 처리
                inoutService.outFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
            } else {
                // 유료 주차장인 경우
                if (parkIn == null || requestParkOutDTO.parkCarType!!.contains("RECOGNIZED")) {
                    // 차량번호로 입차 데이터 미확인, 미인식 인 경우 -> 차량번호 검색
                    inoutService.searchNumberFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, requestParkOutDTO.recognitionResult!!, parkOutDTO.sn!!.toString())
                } else {
                    // 현재 입차 시간 기준으로 정기권 차량 이력 확인
                    seasonTicketService.getLastTicketByVehicleNoAndTicketType(requestParkOutDTO.vehicleNo!!, TicketType.SEASONTICKET)?.let { seasonTicketDTO ->
                        val days = DateUtil.diffDays(requestParkOutDTO.date?: LocalDateTime.now(), seasonTicketDTO.expireDate?: LocalDateTime.now())
                        if ( -3 <= days || days <= 7 ) {
                            // 정기권 정보
                            logger.debug { "extended payment ${seasonTicketDTO.sn} ${seasonTicketDTO.vehicleNo} ${seasonTicketDTO.expireDate}" }
                            inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!)
                        }
                    }?: run {
                       inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!)
                    }

                    if ((price?.totalPrice?: 0) <= 0) {
                        inoutService.outFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
                    }
                }
            }
//            // 1. 유료 주차장인 경우 차량번호로 입차 데이터 미확인, 미인식 인 경우
//            if (parkinglotService.isPaid() && (parkIn == null || requestParkOutDTO.parkCarType!!.contains("RECOGNIZED"))) {
//
//            } else {
//                if (parkinglotService.isPaid() && parkIn != null) {
//                    // 정산 대기 처리
//                    inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!)
//                }
//                // total 0원, 무료 주차장 출차 처리
//                if ( (!parkinglotService.isPaid()) || ( parkinglotService.isPaid() && price?.totalPrice?: 0 <= 0)) {
//                    // 출차 처리
//                    inoutService.outFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
//                }
//            }
            return ResponseEntity.ok(CommonResult.data())

        }?: kotlin.run {
            logger.warn {" ##### 출차 요청 ERROR ${requestParkOutDTO.dtFacilitiesId} gate not found #####"}
            throw CustomException(
                "${requestParkOutDTO.dtFacilitiesId} gate not found",
                ResultCode.FAILED
            )
        }
    }
}