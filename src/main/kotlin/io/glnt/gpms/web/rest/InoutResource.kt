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
import io.glnt.gpms.handler.inout.model.reqSearchParkin
import io.glnt.gpms.handler.rcs.service.RcsService
import io.glnt.gpms.model.criteria.InoutPaymentCriteria
import io.glnt.gpms.model.dto.ReceiptIssuanceDTO
import io.glnt.gpms.model.dto.entity.ParkOutDTO
import io.glnt.gpms.model.dto.entity.ParkinglotVehicleDTO
import io.glnt.gpms.model.dto.RequestParkOutDTO
import io.glnt.gpms.model.dto.TicketInfoDTO
import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.mapper.GateMapper
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.service.*
import mu.KLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import retrofit2.http.Path
import java.lang.Integer.min
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
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

    @RequestMapping(value=["/inouts"], method = [RequestMethod.GET])
    fun getInouts(@RequestParam(name = "startDate", required = false) startDate: String,
                  @RequestParam(name = "endDate", required = false) endDate: String,
                  @RequestParam(name = "searchDateLabel", required = false) searchDateLabel: DisplayMessageClass,
                  @RequestParam(name = "vehicleNo", required = false) vehicleNo: String? = null,
                  @RequestParam(name = "parkCarType", required = false) parkCarType: String? = null,
                  @RequestParam(name = "outSn", required = false) outSn: Long? = null
    ) : ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(
            inoutService.getAllParkLists(
                reqSearchParkin(searchDateLabel = searchDateLabel,
                fromDate = LocalDate.parse(startDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                toDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                searchLabel = vehicleNo?.let { "CARNUM" },
                searchText = vehicleNo,
                parkcartype = parkCarType,
                outSn = outSn
            ))
            ))
    }

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
        inoutService.updateInout(resParkInList)?.let { result ->
            val gateDTO = gateService.findOne(resParkInList.outGateId ?: "")

            // 출차 처리
            parkOutService.findOne(result.outSn!!).ifPresent { parkOutDTO ->
                gateDTO?.let { gate ->
                    if (parkinglotService.isPaid()) {
                        inoutService.waitFacilityIF(
                            "MANPAYMENT",
                            resParkInList.parkCarType,
                            resParkInList.vehicleNo!!,
                            gateMapper.toEntity(gate)!!,
                            parkOutDTO,
                            resParkInList.inDate
                        )
                    }

                    // total 0원, 무료 주차장 출차 처리
                    if ( (!parkinglotService.isPaid()) || ( parkinglotService.isPaid() && (parkOutDTO.payfee?: 0) <= 0)) {
                        // 출차 처리
                        inoutService.outFacilityIF(
                            resParkInList.parkCarType,
                            resParkInList.vehicleNo!!,
                            gateMapper.toEntity(gate)!!,
                            parkInService.findOne(resParkInList.inSn!!)?.let { parkInMapper.toEntity(it) },
                            parkOutDTO.sn!!)
                    }
                }
            }
            return CommonResult.returnResult(CommonResult.data(result))
        }
        throw CustomException(
            "${resParkInList.inSn} transfer(update) failed",
            ResultCode.FAILED
        )

    }

    @RequestMapping(value = ["/inouts/payment"], method = [RequestMethod.GET])
    fun getInoutPayment(@RequestParam(name = "sn", required = false) sn: Long,): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(inoutPaymentService.findByInSn(sn)))
    }

    @RequestMapping(value = ["/inouts/payments"], method = [RequestMethod.GET])
    fun getInoutPayments(@RequestParam(name = "fromDate", required = false) fromDate: String,
                         @RequestParam(name = "toDate", required = false) toDate: String,
                         @RequestParam(name = "resultType", required = false) resultType: ResultType? = null,
                         @RequestParam(name = "limit", required = false) limit: Int? = 0,
                         @RequestParam(name = "vehicleNo", required = false) vehicleNo: String): ResponseEntity<CommonResult> {
        var result = inoutPaymentQueryService.findByCriteria(InoutPaymentCriteria(
                                                                fromDate = DateUtil.stringToLocalDate(fromDate),
                                                                toDate = DateUtil.stringToLocalDate(toDate),
                                                                resultType = resultType,
                                                                vehicleNo = vehicleNo, limit = limit)).filter { it -> (it.amount?: 0) > 0 }
        if ((limit?: 0) > 0) {
            result = result.subList(0, min((limit?: 0) - 1, result.size))
        }
        return CommonResult.returnResult(
//            CommonResult.data(result.filter { it -> it.result != ResultType.WAIT })
            CommonResult.data(result)
        )
    }

    @RequestMapping(value=["/inouts/forced/exit/{sn}"], method = [RequestMethod.DELETE])
    fun forcedExit(@PathVariable sn: Long): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(rcsService.forcedExit(sn))
    }

    @RequestMapping(value = ["/relay/parkIn"], method = [RequestMethod.POST])
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

        parkinglotService.getGateInfoByDtFacilityId(requestParkInDTO.dtFacilitiesId)?.let { gate ->
            // 이미지 사진 저장
            requestParkInDTO.base64Str?.let {
                requestParkInDTO.fileFullPath = inoutService.saveImage(it, requestParkInDTO.vehicleNo, gate.udpGateId?: "")
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
                    delYn = YN.N
                )
            )

            if (!requestParkInDTO.isEmergency!!) {
                // 입차 skip
                // 1. 주차장 운영일 확인
                if (!parkSiteInfoService.checkOperationDay(requestParkInDTO.date)) {
                    logger.warn {" ##### 주차장 운영일이 아님 입차 처리 skip #####"}
                    inoutService.inFacilityIF("RESTRICTE", requestParkInDTO.vehicleNo, gate.gateId, false, false)
                    return ResponseEntity.ok(CommonResult.data())
                }
                // 2. 차량 요일제 확인
                parkSiteInfoService.parkSite!!.vehicleDayOption?.let {
                    if (requestParkInDTO.recognitionResult == "RECOGNITION" && it != VehicleDayType.OFF) {
                        if (DataCheckUtil.isRotation(it, requestParkInDTO.vehicleNo)) {
                        } else {
                            logger.warn {" ##### 입차 차단 요일제적용 차량번호 ${requestParkInDTO.vehicleNo} #####"}
                            inoutService.inFacilityIF("RESTRICTE", requestParkInDTO.vehicleNo, gate.gateId, false, false)
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
                                        inoutService.inFacilityIF("FULL", requestParkInDTO.vehicleNo, gate.gateId, false, false)
                                        return ResponseEntity.ok(CommonResult.data("Full limit $requestParkInDTO.vehicleNo $requestParkInDTO.parkingtype"))
                                    }
                                }

                            }
                        }
                    }
                }
            }

            // 5. 입차 후방 카메라 uuid 매핑
            if ((requestParkInDTO.uuid?: "").isEmpty()) {
//                // 후방 카메라 미인식 skip
//                if (requestParkInDTO.resultcode == "0" || requestParkInDTO.resultcode.toInt() >= 100) { return ResponseEntity.ok(CommonResult.data()) }
//                requestParkInDTO.isSecond = true

                parkInService.getLastByGate(gate.gateId)?.let {
                    if (it.uuid?.isNotEmpty() == true) {
                        if (DateUtil.diffSecs(requestParkInDTO.date, it.inDate!!) < 3) {
//                            requestParkInDTO.beforeParkIn = it
                            requestParkInDTO.uuid = it.uuid
                        }
                    }
                }

//                parkInService.getNoExitVehicleNoAndGateId(requestParkInDTO.vehicleNo, gate.gateId)?.let { ins ->
//                    if (ins.isNotEmpty()) {
//                        logger.warn{" 기 입차 차량번호:${requestParkInDTO.vehicleNo} skip "}
//                        return ResponseEntity.ok(CommonResult.data("Already in $requestParkInDTO.vehicleNo $requestParkInDTO.parkingtype"))
//                    }
//
//                }

            }

            // 입차 skip
            // 4. uuid 동일, 미인식 차량 skip / uuid 동일, 차량번호 동일  기 입차 skip
            parkInService.findOneByUuid(requestParkInDTO.uuid?: "-")?.let { parkInDTOs ->
                if (parkInDTOs.isNotEmpty()) {
                    val parkInDTO = parkInDTOs.sortedByDescending { parkInDTO -> parkInDTO.inDate }[0]
                    requestParkInDTO.beforeParkIn = parkInDTO
                    requestParkInDTO.isSecond = true
                    if (requestParkInDTO.resultcode == "0" || requestParkInDTO.resultcode.toInt() >= 100) { return ResponseEntity.ok(CommonResult.data()) }
                    if (parkInDTO.vehicleNo == requestParkInDTO.vehicleNo) {
                        logger.warn{" 기 입차 차량번호:${requestParkInDTO.vehicleNo} skip "}
                        return ResponseEntity.ok(CommonResult.data("Already in $requestParkInDTO.vehicleNo $requestParkInDTO.parkingtype"))
                    }
                }
            }



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

    @RequestMapping(value = ["/relay/parkOut"], method = [RequestMethod.POST])
    fun parkOut(@Valid @RequestBody requestParkOutDTO: RequestParkOutDTO) : ResponseEntity<CommonResult> {
        logger.warn {" ##### 출차 요청 START #####"}
        logger.warn {" 차량번호 ${requestParkOutDTO.vehicleNo} LPR시설정보 ${requestParkOutDTO.dtFacilitiesId} 입차시간 ${requestParkOutDTO.date} UUID ${requestParkOutDTO.uuid} OCR결과 ${requestParkOutDTO.resultcode}"  }

        parkinglotService.getGateInfoByDtFacilityId(requestParkOutDTO.dtFacilitiesId ?: "")?.let { gate ->
            // 사진 이미지 저장
            requestParkOutDTO.base64Str?.let {
                requestParkOutDTO.fileFullPath = inoutService.saveImage(it, requestParkOutDTO.vehicleNo?: "", gate.udpGateId?: "")
                requestParkOutDTO.fileName = DataCheckUtil.getFileName(requestParkOutDTO.fileFullPath!!)
                requestParkOutDTO.fileUploadId = DateUtil.stringToNowDateTimeMS()+"_F"
            }

            // 출차 skip
            // 1. uuid 동일 기존 출차 데이터가 이미 입차데이터가 매칭 된 경우
            requestParkOutDTO.uuid?.let {  uuid ->
                parkOutService.findByUuid(uuid)?.let {
                    if (it.inSn != null) {
                        throw CustomException(
                            "${requestParkOutDTO.uuid} uuid ${requestParkOutDTO.vehicleNo} is exists", ResultCode.FAILED
                        )
                    }

//                    if (it.parkcartype!!.contains("RECOGNIZED")) {
//                        throw CustomException(
//                            "${requestParkOutDTO.uuid} uuid is exists",
//                            ResultCode.FAILED
//                        )
//                    }
                }
            }

            // 차량 타입 확인
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
                    delYn = YN.N
                )
            )

            // 입차 확인
            val parkIn =
                if (requestParkOutDTO.parkCarType != "UNRECOGNIZED")
                    parkInService.getLastVehicleNoByDate(requestParkOutDTO.vehicleNo?: "", requestParkOutDTO.date!!) else null

            // 기존 출차 데이터 확인
            val existsParkOut = parkIn?.sn?.let { sn ->
                parkOutService.findByInSn(sn).orElse(null)
            }
//                ?: kotlin.run {
//                // One way 등 후진등으로 인하여 재출차 요청 시 삭제 요청 by 2022-01-11 QA
//                if (requestParkOutDTO.recognitionResult == "RECOGNITION") {
//                    parkOutService.findByLastVehicleNo(requestParkOutDTO.vehicleNo ?: "", gate.gateId).orElse(null)?.let { parkOutDTO ->
//                        if (DateUtil.diffMins(parkOutDTO.outDate ?: LocalDateTime.now(), requestParkOutDTO.date?: LocalDateTime.now()) < 3) {
//                            logger.warn { "출차 후 재출차 3분 미만 차단기 open 처리 ${requestParkOutDTO.vehicleNo} 기존 출차 시간 ${parkOutDTO.outDate} 현재 출차 시간 ${requestParkOutDTO.date}" }
//                            relayClient.sendActionBreaker(gate.gateId, "open")
//                            return ResponseEntity.ok(CommonResult.data())
//                        }
//                    }
//                }
//                null
//            }
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
            var isLegTime: Boolean = false
            // 유료 주차장인 경우 요금 계산
            if (parkIn!= null && parkinglotService.isPaid() && requestParkOutDTO.parkCarType != "UNRECOGNIZED") {
                // 사전 정산 시 inout-payment 데이터 확인 후 legTime 이후 out_date 이면 시간만큼 요금 계산
                inoutPaymentService.getLastInSnAndResult(parkIn.sn ?: -1, ResultType.SUCCESS)?.let { prePayment ->
                    fareRefService.getFareBasic()?.let { cgBasic ->
                        prePrice = BasicPrice(
                            orgTotalPrice = prePayment.parkFee,
                            parkTime = prePayment.parkTime ?: 0,
                            totalPrice = prePayment.amount ?: 0,
                            discountPrice = prePayment.discount ?: 0,
                            dayilyMaxDiscount = prePayment.dayDiscount ?: 0)

                        val diffMins = DateUtil.diffMins(DateUtil.stringToLocalDateTime(prePayment.approveDateTime!!), requestParkOutDTO.date ?: LocalDateTime.now())
                        isLegTime = diffMins <= (cgBasic.legTime ?: 0)
                        if (!isLegTime) {
                            price = inoutService.calcParkFee("RECALC", DateUtil.stringToLocalDateTime(prePayment.approveDateTime!!), requestParkOutDTO.date!!, VehicleType.SMALL, requestParkOutDTO.vehicleNo ?: "", parkIn.sn ?: -1)
                        }
                    }
                }?: kotlin.run {
                    price = inoutService.calcParkFee("OUT", parkIn.inDate!!, requestParkOutDTO.date!!, VehicleType.SMALL, requestParkOutDTO.vehicleNo ?: "", parkIn.sn ?: -1)
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
                delYn = YN.N ,
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
                    if (parkOutDTO.parkcartype!!.contains("TICKET")) {
                        // 입차 데이터 미확인, 정기권 차량인 경우 출차 시킴(2022-01-05)
                        inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, requestParkOutDTO.date!!, null, null, isLegTime)
                        inoutService.outFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkIn, parkOutDTO.sn!!)
                    } else {
                        inoutService.searchNumberFacilityIF(requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, requestParkOutDTO.recognitionResult!!, parkOutDTO.sn!!.toString())
                    }
                } else {
                    // 현재 입차 시간 기준으로 정기권 차량 이력 확인
                    seasonTicketService.getLastTicketByVehicleNoAndTicketType(requestParkOutDTO.vehicleNo!!, TicketType.SEASONTICKET)?.let { seasonTicketDTO ->
                        val days = DateUtil.diffDays(requestParkOutDTO.date?: LocalDateTime.now(), seasonTicketDTO.expireDate?: LocalDateTime.now())
                        if ( -3 <= days || days <= 7 ) {
                            // 정기권 정보
                            logger.debug { "extended payment ${seasonTicketDTO.sn} ${seasonTicketDTO.vehicleNo} ${seasonTicketDTO.expireDate}" }
                            val ticketInfo = TicketInfoDTO(
                                sn = seasonTicketDTO.sn.toString(),
                                effectDate = DateUtil.LocalDateTimeToDateString(DateUtil.beginTimeToLocalDateTime(DateUtil.LocalDateTimeToDateString(DateUtil.getAddDays(seasonTicketDTO.expireDate?: LocalDateTime.now(), 1)))),
                                expireDate = DateUtil.LocalDateTimeToDateString(DateUtil.getAddMonths(seasonTicketDTO.expireDate?: LocalDateTime.now(), 1)),
                                name = seasonTicketDTO.ticket?.ticketName ?: kotlin.run { "정기권" },
                                price = seasonTicketDTO.ticket?.price.toString() ?: kotlin.run { "0" }
                            )
                            inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!, null, ticketInfo, isLegTime)
                        } else {
                            inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!, null, null, isLegTime)
                        }
                    }?: run {
                       inoutService.waitFacilityIF("PAYMENT", requestParkOutDTO.parkCarType!!, requestParkOutDTO.vehicleNo!!, gate, parkOutDTO, parkIn.inDate!!,null, null, isLegTime)
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

    @RequestMapping(value = ["/inouts/payment/receipt/{sn}/print/{facilityId}"], method = [RequestMethod.GET])
    fun printReceipt(@PathVariable sn: Long, @PathVariable facilityId: String): ResponseEntity<CommonResult> {
        val inoutPayment = inoutPaymentService.findOne(sn).orElse(null)
        inoutPayment?.let { inoutPaymentDTO ->
            val receiptIssuance = ReceiptIssuanceDTO(
                vehicleNumber = inoutPaymentDTO.vehicleNo,
                inVehicleDateTime = DateUtil.formatDateTime(parkInService.findOne(inoutPaymentDTO.inSn?: 0)?.inDate?: LocalDateTime.now(), "yyyy-MM-dd HH:mm"),
                parkingTimes = inoutPaymentDTO.parkTime.toString(),
                parkingAmount = inoutPaymentDTO.parkFee.toString(),
                discountAmount = ((inoutPaymentDTO.discount?: 0) + (inoutPaymentDTO.dayDiscount?: 0)).toString(),
                adjustmentAmount = ((inoutPaymentDTO.parkFee?: 0) - ((inoutPaymentDTO.discount?: 0) + (inoutPaymentDTO.dayDiscount?: 0))).toString(),
                cardNumber = inoutPaymentDTO.cardNumber,
                cardCorp = inoutPaymentDTO.cardCorp,
                transactionId = inoutPaymentDTO.transactionId,
                adjustmentDateTime = DateUtil.formatDateTime(DateUtil.stringToLocalDateTime(inoutPaymentDTO.approveDateTime?: ""),"yyyy-MM-dd HH:mm:ss")
            )
            relayClient.sendPayStation(
                parkinglotService.getGateInfoByDtFacilityId(facilityId ?: "")?.gateId?: "",
                "receiptIssuance",
                inoutPaymentDTO.sn.toString(),
                receiptIssuance,
                facilityId
            )
            return ResponseEntity.ok(CommonResult.data())
        }?: run {
            throw CustomException(
                "$sn payment not found",
                ResultCode.FAILED
            )
        }
    }
}