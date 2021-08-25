package io.glnt.gpms.web.rest

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.configs.ApiConfig
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.service.CorpQueryService
import io.glnt.gpms.model.dto.CorpCriteria
import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.service.CorpService
import io.glnt.gpms.service.InoutDiscountService
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.springframework.http.ResponseEntity
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.*
import java.util.HashMap
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
    private val inoutDiscountService: InoutDiscountService
){
    companion object : KLogging()

    @RequestMapping(value = ["/corps"], method = [RequestMethod.GET])
    fun getStores(criteria: CorpCriteria): ResponseEntity<CommonResult> {
        return CommonResult.returnResult(CommonResult.data(corpQueryService.findByCriteria(criteria)))
    }

    @RequestMapping(value = ["/corps"], method = [RequestMethod.POST])
    fun createStore(@Valid @RequestBody corpDTO: CorpDTO): ResponseEntity<CommonResult> {
        if (corpDTO.sn != null) {
            throw CustomException(
                "corp create sn exists",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpService.save(corpDTO, "create", parkSiteInfoService.getSiteId())))
    }

    @RequestMapping(value = ["/corps"], method = [RequestMethod.PUT])
    fun updateStore(@Valid @RequestBody corpDTO: CorpDTO): ResponseEntity<CommonResult> {
        if (corpDTO.sn == null) {
            throw CustomException(
                "corp update not found sn",
                ResultCode.FAILED
            )
        }
        return CommonResult.returnResult(CommonResult.data(corpService.save(corpDTO, "update", parkSiteInfoService.getSiteId())))
    }

    @RequestMapping(value = ["/corps/{sn}/{inSn}/able/ticket"], method = [RequestMethod.GET])
    fun getStoreAbleTickets(@PathVariable sn: Long, @PathVariable inSn: String): ResponseEntity<CommonResult> {
        logger.debug { "store fetch able ticket" }
        val tickets = corpService.getStoreTicketsByStoreSn(sn)
        if (inSn == "ALL") {
            tickets.forEach{ ticket ->
                ticket.todayUse = discountService.getTodayUseDiscountTicket(sn, ticket.discountClassSn!!)
                ticket.totalCnt = ticket.totalQuantity
                ticket.ableCnt = ticket.totalQuantity!! - ticket.useQuantity!!
            }
            return CommonResult.returnResult(CommonResult.data(tickets))
        }

        tickets.forEach{ ticket ->
            corpService.getCorpTicketHistByTicketSn(ticket.sn!!)?.let {
                ticket.todayUse = discountService.getTodayUseDiscountTicket(sn, ticket.discountClassSn!!)
                ticket.totalCnt = ticket.totalQuantity!! - ticket.useQuantity!!
                val ableCnt = inoutDiscountService.ableDiscountCntByInSn(inSn.toLong(), ticket.discountClass!!)?: 0
                ticket.ableCnt = if (ableCnt > ticket.totalQuantity!! - ticket.useQuantity!!) ticket.totalQuantity!! - ticket.useQuantity!! else ableCnt
            }
        }

        return CommonResult.returnResult(CommonResult.data(tickets))
    }

}