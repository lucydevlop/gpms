package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.criteria.InoutPaymentCriteria
import io.glnt.gpms.model.dto.request.resParkInList
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.mapper.GateMapper
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.service.*
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class InoutResource (
    private val inoutService: InoutService,
    private val parkinglotService: ParkinglotService,
    private val gateService: GateService,
    private val gateMapper: GateMapper,
    private val parkOutService: ParkOutService,
    private val parkInService: ParkInService,
    private val parkInMapper: ParkInMapper,
    private val inoutPaymentQueryService: InoutPaymentQueryService
){
    companion object : KLogging()

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
                        "PAYMENT",
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
                         @RequestParam(name = "toDate", required = false) toDate: String): ResponseEntity<CommonResult> {
        val result = inoutPaymentQueryService.findByCriteria(InoutPaymentCriteria(
                                                                fromDate = DateUtil.stringToLocalDate(fromDate),
                                                                toDate = DateUtil.stringToLocalDate(toDate)))
        return CommonResult.returnResult(
            CommonResult.data(result.filter { it -> it.result != ResultType.WAIT })
        )
    }
}