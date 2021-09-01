package io.glnt.gpms.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.common.model.reqParkingDiscountSearchTicket
import io.glnt.gpms.handler.dashboard.user.model.ResDiscountTicetsApplyList
import io.glnt.gpms.handler.dashboard.user.model.reqParkingDiscountApplyTicketSearch
import io.glnt.gpms.handler.discount.model.reqApplyInoutDiscountSearch
import io.glnt.gpms.handler.discount.service.DiscountService
import io.glnt.gpms.model.dto.CorpTicketClassDTO
import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.entity.InoutDiscount
import io.glnt.gpms.model.mapper.CorpTicketClassMapper
import io.glnt.gpms.model.repository.CorpTicketClassRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CorpTicketClassService(
    private val corpTicketClassRepository: CorpTicketClassRepository,
    private val corpTicketClassMapper: CorpTicketClassMapper,
    private val discountService: DiscountService
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<CorpTicketClassDTO> {
        return corpTicketClassRepository.findAll().map(corpTicketClassMapper::toDTO)
    }

    fun findBySn(sn: Long): CorpTicketClassDTO {
        return corpTicketClassMapper.toDTO(corpTicketClassRepository.findBySn(sn))
    }

    fun save(corpTicketClassDTO: CorpTicketClassDTO): CorpTicketClassDTO {
        logger.debug("Request to save CorpTicketClass : $corpTicketClassDTO")
        val corpTicketClass = corpTicketClassMapper.toEntity(corpTicketClassDTO)
        corpTicketClassRepository.save(corpTicketClass!!)

        return corpTicketClassMapper.toDTO(corpTicketClass)
    }

    fun parkingDiscountSearchApplyTicket(request: reqParkingDiscountApplyTicketSearch) : CommonResult {
        try {
            val tickets = discountService.searchCorpTicketByCorp(
                reqParkingDiscountSearchTicket(
                    searchLabel = "CORPSN",
                    searchText = request.corpSn.toString()
                )
            )
            when (tickets.code) {
                ResultCode.SUCCESS.getCode() -> {
                    val result = ArrayList<ResDiscountTicetsApplyList>()
                    val lists = tickets.data as List<CorpTicketInfo>
                    lists.forEach { it ->
                        if ( (request.discountClassSn != null && it.classSn == request.discountClassSn) || request.discountClassSn == null ) {
                            discountService.searchInoutDiscount(
                                reqApplyInoutDiscountSearch(
                                    ticketSn = it.sn!!, startDate = request.startDate, endDate = request.endDate,
                                    applyStatus = request.applyStatus, ticketClassSn = request.ticketClassSn
                                )
                            )?.let {
                                its ->
                                its.forEach {
                                    result.add(
                                        ResDiscountTicetsApplyList(
                                            sn = it.sn!!,
                                            vehicleNo = it.parkIn!!.vehicleNo!!,
                                            discountType = it.discontType!!,
                                            discountClassSn = it.discountClassSn!!,
                                            //discountNm = it.ticketHist!!.ticketInfo!!.discountClass!!.discountNm,
                                            discountNm = it.discountClass.discountNm,
                                            calcYn = it.calcYn!!,
                                            delYn = it.delYn!!,
                                            createDate = it.createDate!!,
                                            quantity = it.quantity!!,
                                            ticketClassSn = it.ticketClassSn
                                        )
                                    )
                                }
                            }
                        }
                    }
                    return CommonResult.data(result.sortedByDescending { it.createDate })
                }
            }
            return CommonResult.data()
        }catch (e: CustomException) {
            logger.error { "parkingDiscountSearchApplyTicket failed $e" }
            return CommonResult.Companion.error("parkingDiscountSearchApplyTicket failed ${e.message}")
        }
    }
}