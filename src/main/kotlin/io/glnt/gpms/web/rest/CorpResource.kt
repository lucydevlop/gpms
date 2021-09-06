package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.user.model.ResDiscountTicetsApplyList
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.model.dto.*
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.service.*
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap
import javax.validation.Valid

@RestController
@RequestMapping(
    path = ["/${ApiConfig.API_VERSION}"]
)
@CrossOrigin(origins = arrayOf("*"), allowedHeaders = arrayOf("*"))
class CorpResource (
    private val corpService: CorpService,
    private val corpQueryService: CorpQueryService,
    private val parkSiteInfoService: ParkSiteInfoService,
    private val discountService: DiscountService,
    private val inoutDiscountService: InoutDiscountService,
    private val corpTicketClassService: CorpTicketClassService,
    private val inoutDiscountQueryService: InoutDiscountQueryService
){
    companion object : KLogging()

    @RequestMapping(value = ["/corps"], method = [RequestMethod.GET])
    fun getCorps(criteria: CorpCriteria): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(corpQueryService.findByCriteria(criteria)))
    }

    @RequestMapping(value = ["/corps"], method = [RequestMethod.POST])
    fun createCorp(@Valid @RequestBody corpDTO: CorpDTO): ResponseEntity<CommonResult> {
        if (corpDTO.sn != null) {
            throw CustomException(
                "corp create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpService.save(corpDTO, "create", parkSiteInfoService.getSiteId())))
    }

    @RequestMapping(value = ["/corps"], method = [RequestMethod.PUT])
    fun updateCorp(@Valid @RequestBody corpDTO: CorpDTO): ResponseEntity<CommonResult> {
        if (corpDTO.sn == null) {
            throw CustomException(
                "corp update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpService.save(corpDTO, "update", parkSiteInfoService.getSiteId())))
    }

    @RequestMapping(value = ["/corps/{sn}/{inSn}/able/ticket"], method = [RequestMethod.GET])
    fun getCorpAbleTickets(@PathVariable sn: Long, @PathVariable inSn: String): ResponseEntity<CommonResult> {
        logger.debug { "store fetch able ticket" }
        val tickets = corpService.getCorpTicketsByCorpSn(sn).filter { t -> t.delYn == DelYn.N }
        if (inSn == "ALL") {
            tickets.forEach{ ticket ->
                ticket.todayUse = discountService.getTodayUseDiscountTicket(sn, ticket.corpTicketClass!!.sn!!)
                ticket.totalCnt = ticket.totalQuantity
                ticket.ableCnt = ticket.totalQuantity!! - ticket.useQuantity!!
            }
            return CommonResult.returnResult(CommonResult.data(tickets))
        }

        tickets.forEach{ ticket ->
            corpService.getCorpTicketHistByTicketSn(ticket.sn!!)?.let {
                ticket.todayUse = discountService.getTodayUseDiscountTicket(sn, ticket.corpTicketClass!!.sn!!)
                ticket.totalCnt = ticket.totalQuantity!! - ticket.useQuantity!!
                val ableCnt = inoutDiscountService.ableDiscountCntByInSn(inSn.toLong(), ticket.corpTicketClass!!)?: 0
                ticket.ableCnt = if (ableCnt > ticket.totalQuantity!! - ticket.useQuantity!!) ticket.totalQuantity!! - ticket.useQuantity!! else ableCnt
            }
        }

        return CommonResult.returnResult(CommonResult.data(tickets))
    }

    @RequestMapping(value = ["/corps/{sn}/tickets/info"], method = [RequestMethod.GET])
    fun getCorpTicketsSummary(@PathVariable sn: String): ResponseEntity<CommonResult> {
        logger.debug { "corp fetch ticket summary" }
        var summarys = ArrayList<CorpTicketSummaryDTO>()

        val corpTicketClass = corpTicketClassService.findAll().filter { corpTicketClassDTO -> corpTicketClassDTO.delYn == DelYn.N }

        if (sn == "ALL") {
            val tickets = corpService.getAllCorpTickets()
            val corps = corpQueryService.findByCriteria(CorpCriteria(delYn = DelYn.N))
            corps.forEach { corpDTO ->
                val corpTickets = tickets.filter { corpTicketDTO -> corpTicketDTO.corpSn == corpDTO.sn}
                val results = ArrayList<HashMap<String, Any?>>()
                for (i in corpTicketClass.indices) {
                    val corpTicket = corpTickets.filter { it -> it.corpTicketClass!!.sn == corpTicketClass[i].sn }
                    results.add(hashMapOf<String, Any?>(
                        "id" to i,
                        "title" to corpTicketClass[i].name,
                        "total" to corpTicket.sumOf { it -> it.totalQuantity ?: 0 },
                        "use" to corpTicket.sumOf { it -> it.useQuantity ?: 0 }
                    ))
                }
                summarys.add(CorpTicketSummaryDTO(corp = corpDTO, tickets = results))
            }
            return CommonResult.returnResult(CommonResult.data(summarys))
        } else {
            val tickets = corpService.getCorpTicketsByCorpSn(sn.toLong())
            val corps = corpQueryService.findByCriteria(CorpCriteria(delYn = DelYn.N, sn = sn.toLong()))
            val results = ArrayList<HashMap<String, Any?>>()
            for (i in corpTicketClass.indices) {
                val corpTicket = tickets.filter { it -> it.corpTicketClass!!.sn == corpTicketClass[i].sn }
                results.add(hashMapOf<String, Any?>(
                    "id" to i,
                    "title" to corpTicketClass[i].name,
                    "total" to corpTicket.sumOf { it -> it.totalQuantity ?: 0 },
                    "use" to corpTicket.sumOf { it -> it.useQuantity ?: 0 }
                ))
            }
            return CommonResult.returnResult(CommonResult.data(CorpTicketSummaryDTO(corp = corps[0], tickets = results)))
        }
    }

    @RequestMapping(value = ["/corps/add/tickets"], method = [RequestMethod.POST])
    fun addCorpTickets(@Valid @RequestBody addCorpTicketDTO: AddCorpTicketDTO): ResponseEntity<CommonResult> {
        logger.debug { "corp add ticket $addCorpTicketDTO" }
        corpService.addCorpTickets(addCorpTicketDTO)
        return getCorpTicketsSummary(addCorpTicketDTO.corpSn.toString())
    }

    @RequestMapping(value = ["/corps/{sn}/tickets"], method = [RequestMethod.GET])
    fun getCorpsApplyTickets(@PathVariable sn: String,
                             @RequestParam(name = "fromDate", required = false) fromDate: String,
                             @RequestParam(name = "toDate", required = false) toDate: String,
                             @RequestParam(name = "ticketClassSn", required = false) ticketClassSn: Long? = null
    ): ResponseEntity<CommonResult> {
        logger.debug { "corp get ticket $sn $fromDate, $toDate, $ticketClassSn" }
        val result = ArrayList<ResDiscountTicetsApplyList>()
        val inoutDiscounts = inoutDiscountQueryService.findByCriteria(
                                    InoutDiscountCriteria(corpSn = sn.toLong(),
                                                          fromDate = DateUtil.stringToLocalDate(fromDate),
                                                          toDate = DateUtil.stringToLocalDate(toDate),
                                                          ticketClassSn = ticketClassSn))
        inoutDiscounts.forEach {
            result.add(
                ResDiscountTicetsApplyList(
                    sn = it.sn!!,
                    vehicleNo = it.parkInDTO!!.vehicleNo!!,
                    discountType = it.discontType!!,
                    discountClassSn = it.discountClassSn!!,
                    discountNm = it.discountClass!!.discountNm!!,
                    calcYn = it.calcYn!!,
                    delYn = it.delYn!!,
                    createDate = it.createDate!!,
                    quantity = it.quantity!!,
                    ticketClassSn = it.ticketClassSn!!
                )
            )
        }
        return CommonResult.returnResult(CommonResult.data(result))
    }

}