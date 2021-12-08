package io.glnt.gpms.handler.external.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.exception.CustomException
//import io.glnt.gpms.handler.corp.service.CorpService
import io.glnt.gpms.handler.product.service.ProductService
import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.service.CorpService
import io.glnt.gpms.service.ParkSiteInfoService
import mu.KLogging
import org.springframework.stereotype.Service


@Service
class externalService(
    private var productService: ProductService,
    private var corpService: CorpService,
    private var parkSiteInfoService: ParkSiteInfoService
) {
    companion object : KLogging()

    @Throws(CustomException::class)
    fun searchTickets(request: reqSearchProductTicket) : CommonResult {
        try {
            productService.getProducts(request)?.let { tickets ->
                return CommonResult.data(tickets)
            } ?: run {
                return CommonResult.notfound("external search ticket not found ${request} ")
            }
//            when (data.code) {
//                ResultCode.SUCCESS.getCode() -> {
//                    val tickets : List<ProductTicket> = data.data as? List<ProductTicket> ?: emptyList()
//                    if (tickets.isNotEmpty()) {
//                        return CommonResult.data(data.data)
//                    } else {
//                        return CommonResult.notfound("external search ticket not found ${request} ")
//                    }
//                }
//                else -> {
//                    return CommonResult.error(data.msg!!)
//                }
//            }
        }catch (e: CustomException){
            logger.error { "external searchTickets failed $e" }
            return CommonResult.error("external searchTickets failed $e")
        }
    }

    @Throws(CustomException::class)
    fun createTickets(request: ArrayList<reqCreateProductTicket>) : CommonResult {
        try {
            request.forEach { ticket ->
                ticket.corpName?.let { text ->
                    val exist = corpService.getStoreByCorpName(text)
                    exist?.let {
                        ticket.corpSn = it.sn
                    }?: kotlin.run {
                        val corp = CorpDTO(corpName = ticket.corpName, ceoName = ticket.ceoName, delYn = DelYn.N, tel = (ticket.corpTel?: "").replace("-", "") )
                        corpService.save(corp, "create", parkSiteInfoService.getSiteId()).let { corpDTO ->
                            ticket.corpSn = corpDTO.sn
                        }
                    }
//                    if (exist == null) {
//                        val corp = CorpDTO(corpName = ticket.corpName, ceoName = ticket.ceoName, delYn = DelYn.N, tel = (ticket.corpTel?: "").replace("-", "") )
//                        corpService.save(corp, "create", parkSiteInfoService.getSiteId()).let { corpDTO ->
//                            ticket.corpSn = corpDTO.sn
//                        }
//                    } else {
//
//
//                    }
                }
                val data = productService.createProduct(ticket)

                when (data.code) {
                    ResultCode.SUCCESS.getCode() -> {
                    }
                    else -> {
                        return CommonResult.error(data.msg!!)
                    }
                }
            }

        }catch (e: CustomException){
            logger.error { "external createProductTicket failed $e" }
            return CommonResult.error("external createProductTicket failed $e")
        }
        return CommonResult.data()
    }

    @Throws(CustomException::class)
    fun deleteTickets(request: ArrayList<reqCreateProductTicket>): CommonResult {
        try {
            request.forEach { it ->
                productService.getProducts(
                                reqSearchProductTicket(searchLabel="CARNUM",
                                                       searchText= it.vehicleNo,
                                                       expireDate = it.expireDate,
                                                       effectDate = it.effectDate, delYn = "N"
                                )
                )?.let { tickets ->
                    tickets.forEach { ticket->
                        productService.deleteTicket(ticket.sn!!)
                    }
                } ?: run {
                    return CommonResult.notfound("external delete ticket not found ${it.vehicleNo} ")
                }

//
//
//                when (data.code) {
//                    ResultCode.SUCCESS.getCode() -> {
//                        val tickets : List<ProductTicket> = data.data as? List<ProductTicket> ?: emptyList()
//                        if (tickets.isNotEmpty()) {
//                            tickets.forEach { ticket->
//                                productService.deleteTicket(ticket.sn!!)
//                            }
//                        } else {
//                            return CommonResult.notfound("external delete ticket not found ${it.vehicleNo} ")
//                        }
//                    }
//                    else -> {
//                        return CommonResult.error(data.msg!!)
//                    }
//                }
            }

        }catch (e: CustomException){
            logger.error { "external deleteTickets failed $e" }
            return CommonResult.error("external deleteTickets failed $e")
        }
        return CommonResult.data()
    }
}