package io.glnt.gpms.handler.discount.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.api.ResultCode
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.corp.service.CorpService
import io.glnt.gpms.handler.dashboard.admin.model.reqCreateCorpTicket
import io.glnt.gpms.handler.dashboard.admin.model.reqSearchCorp
import io.glnt.gpms.handler.dashboard.common.model.reqParkingDiscountSearchTicket
import io.glnt.gpms.handler.discount.model.*
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DiscountRangeType
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.repository.CorpTicketHistoryRepository
import io.glnt.gpms.model.repository.CorpTicketRepository
import io.glnt.gpms.model.repository.DiscountClassRepository
import io.glnt.gpms.model.repository.InoutDiscountRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
class DiscountService {
    companion object : KLogging()

    @Autowired
    lateinit var corpService: CorpService

    @Autowired
    private lateinit var discountClassRepository: DiscountClassRepository

    @Autowired
    private lateinit var corpTicketRepository: CorpTicketRepository

    @Autowired
    private lateinit var corpTicketHistoryRepository: CorpTicketHistoryRepository

    @Autowired
    private lateinit var inoutDiscountRepository: InoutDiscountRepository

    fun getDiscountClass() : CommonResult {
        return CommonResult.data(discountClassRepository.findAll())
    }

    fun createDiscountClass(request: DiscountClass): CommonResult {
        logger.info { "createDiscountClass $request" }
        try {
            discountClassRepository.save(request)
            return CommonResult.data(getDiscountClass())
        }catch (e: RuntimeException) {
            logger.error { "createDiscountClass error ${e.message}" }
            return CommonResult.Companion.error("tb_corpclass create failed")
        }
    }

    fun searchCorpTicketByCorp(request: reqParkingDiscountSearchTicket) : CommonResult {
        try{
            val result = ArrayList<Any>()
            request.searchLabel?.let {
                val data = corpService.getCorp(reqSearchCorp(searchLabel = it, searchText = request.searchText))
                when(data.code) {
                    ResultCode.SUCCESS.getCode() -> {
                        val corps: List<Corp> = data.data as? List<Corp> ?: emptyList()
                        if (corps.isNotEmpty()) {
                            corps.forEach {
                                val tickets = corpTicketRepository.findByCorpSnAndDelYn(it.sn!!, DelYn.N)
                                if (tickets!!.isNotEmpty()) {
                                    tickets.forEach {
                                        result.add(it)
                                    }

                                }
                            }
                        }
                    }
                }
                return CommonResult.data(result)
            } ?: run {
                corpTicketRepository.findAll().let {
                    return CommonResult.data(it)
                }
            }
        }catch (e: CustomException){
            logger.error { "searchCorpTicketByCorp error $e" }
            return CommonResult.error("searchCorpTicketByCorp search failed")
        }
    }

    fun searchCorpTicketByCorpAndDiscountClass(corpSn: Long, discountClassNo: Long): CommonResult {
        try{
            return CommonResult.data(corpTicketRepository.findByCorpSnAndDiscountClassSnAndDelYn(corpSn, discountClassNo, DelYn.N))

        }catch (e: CustomException){
            logger.error { "searchCorpTicketByCorpAndDiscountClass error $e" }
            return CommonResult.error("searchCorpTicketByCorpAndDiscountClass search failed")
        }
    }

    @Throws(CustomException::class)
    fun createCorpTicket(request: reqCreateCorpTicket): CommonResult {
        try {
            val corpTicket = corpTicketRepository.findByCorpSnAndDiscountClassSnAndDelYn(request.corpSn, request.discountClassSn, DelYn.N)?.let {
                it.totalQuantity = it.totalQuantity.plus(request.quantity!!)
                corpTicketRepository.save(it)
            } ?: run {
                corpTicketRepository.save(
                    CorpTicketInfo(sn = null, corpSn = request.corpSn, discountClassSn = request.discountClassSn,
                        totalQuantity = request.quantity!!, useQuantity = 0, delYn = DelYn.N)
                )
            }

            corpTicketHistoryRepository.save(
                CorpTicketHistory(sn = null, ticketSn = corpTicket.sn!!, totalQuantity = request.quantity!!,
                    effectDate = LocalDateTime.now(), delYn = DelYn.N)
            )
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "createCorpTicket error $e" }
            return CommonResult.error("createCorpTicket failed")
        }
    }

    fun updateCorpTicketInfo(data: CorpTicketInfo) : CorpTicketInfo {
        return corpTicketRepository.save(data)
    }

    fun getDiscountableTicketsBySn(ticketSn: Long): CorpTicketHistory? {
        return corpTicketHistoryRepository.findTopByTicket(ticketSn)
    }

    fun updateCorpTicketHistory(data: CorpTicketHistory): CorpTicketHistory {
        return corpTicketHistoryRepository.save(data)
    }



    fun getDiscountableTickets(request: reqDiscountableTicket): CommonResult {
        logger.info { "getDiscountableTickets $request" }
        try {
            val tickets = searchCorpTicketByCorp(reqParkingDiscountSearchTicket(searchLabel = "CORPSN", searchText=request.corpSn.toString()))
            val weekRange = DateUtil.getWeekRange(DateUtil.formatDateTime(request.date!!, "yyyy-MM-dd"))

            if (tickets.code == ResultCode.SUCCESS.getCode() && tickets.data != null) {
                val data = ArrayList<CorpTicketInfo>()


                for (ticket in tickets.data as List<CorpTicketInfo>) {
                    corpTicketHistoryRepository.findByTicketSnAndDelYn(ticket.sn!!, DelYn.N)?.let { hist ->
                        ticket.ableCnt = getAbleDiscountCnt(reqSearchInoutDiscount(ticketSn = ticket.discountClass!!.sn!!, inSn = request.inSn!!))
                        if (ticket.ableCnt != null) {
                            if (ticket.ableCnt!! > 0 ) data.add(ticket)
                        }
                    }

                }

                val result = data.filter {
                    // 입차 요일 확인
                    ( it.discountClass!!.dayRange == weekRange || it.discountClass!!.dayRange == DiscountRangeType.ALL)
                }
                if (result.isNullOrEmpty()) return CommonResult.data()
                return CommonResult.data(result)
            }
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "getDiscountableTickets error ${e.message}" }
            return CommonResult.error("getDiscountableTickets search failed")
        }
    }

    fun getAbleDiscountCnt(request: reqSearchInoutDiscount) : Int? {
        logger.info { "getAbleDiscountCnt $request" }
        try {
            val result = ArrayList<Int>()
            discountClassRepository.findBySn(request.ticketSn).let { discountClass ->
                inoutDiscountRepository.findByInSnAndDiscountClassSnAndDelYn(request.inSn, request.ticketSn, DelYn.N)?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > discountClass.disMaxNo!!) return 0
                    if (it == 0) result.add(discountClass.disMaxNo!!) else  result.add(discountClass.disMaxNo!!-it)
                }
                inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
                    request.ticketSn, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > discountClass.disMaxDay!!) return 0
                    if (it == 0) result.add(discountClass.disMaxDay!!) else  result.add(discountClass.disMaxDay!!-it)
                }
                inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
                    request.ticketSn, DateUtil.firstDayToLocalDateTime(DateUtil.nowDate), DateUtil.lastDayToLocalDateTime(DateUtil.nowDate), DelYn.N )?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > discountClass.disMaxMonth!!) return 0
                    if (it == 0) result.add(discountClass.disMaxMonth!!) else  result.add(discountClass.disMaxMonth!!-it)
                }
            }
            return result.min()
        }catch (e: CustomException){
            logger.error { "getInoutDiscount error ${e.message}" }
            return null//CommonResult.error("getInoutDiscount search failed")
        }
    }

    fun addInoutDiscount(request: reqAddInoutDiscount): InoutDiscount {
        return inoutDiscountRepository.save(
            InoutDiscount(sn = null, discontType = request.discountType, discountClassSn = request.discountClassSn,
                          ticketHistSn = request.ticketSn, inSn = request.inSn, quantity = request.quantity, delYn = DelYn.N, corpSn = request.corpSn))
    }

    fun saveInoutDiscount(discount: InoutDiscount) : InoutDiscount {
        return inoutDiscountRepository.save(discount)
    }

    fun applyInoutDiscount(inSn: Long) {
        try {
            inoutDiscountRepository.findByInSnAndDelYnAndCalcYn(inSn, DelYn.N, DelYn.Y)?.let { discounts ->
                discounts.forEach { it ->
                    it.applyDate = LocalDateTime.now()
                    saveInoutDiscount(it)
                    inoutDiscountRepository.flush()
                }
            }
        }catch (e: CustomException) {
            logger.error { "applyInoutDiscount error $e" }
        }
    }

    fun searchInoutDiscount(inSn: Long) : List<InoutDiscount>? {
        return inoutDiscountRepository.findByInSnAndDelYn(inSn, DelYn.N)
    }

    fun searchInoutDiscount(request: reqApplyInoutDiscountSearch): List<InoutDiscount>? {
        try{
            val ticketHists = ArrayList<Long>()
            corpTicketHistoryRepository.findByTicketSnAndDelYn(request.ticketSn, DelYn.N)?.let { tickets ->
                tickets.forEach { ticket ->
                    ticketHists.add(ticket.sn!!)
                }
            }
            request.ticketsSn = ticketHists
            return inoutDiscountRepository.findAll(findAllInoutDiscountSpecification(request))

        }catch (e: CustomException) {
            logger.error { "searchInoutDiscount error $e" }
        }
        return null
    }

    fun findAllInoutDiscountSpecification(request: reqApplyInoutDiscountSearch): Specification<InoutDiscount> {
        val spec = Specification<InoutDiscount> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            clues.add(
                //criteriaBuilder.and(criteriaBuilder.`in`(root.get<Long>("ticketHistSn")), request.ticketsSn)
                criteriaBuilder.and(root.get<Long>("ticketHistSn").`in`(request.ticketsSn!!.map { it }))
            )

            clues.add(
                criteriaBuilder.between(
                    root.get("createDate"),
                    DateUtil.beginTimeToLocalDateTime(request.startDate.toString()),
                    DateUtil.lastTimeToLocalDateTime(request.endDate.toString())
                )
            )
//            if (request.ticketType != TicketType.ALL) {
//                clues.add(
//                    criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("ticketType")), request.ticketType.code)
//                )
//            }
            if (request.applyStatus != null) {
                when (request.applyStatus) {
                    "DO" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("calcYn")), DelYn.N)
                        )
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N)
                        )
                    }
                    "DONE" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("calcYn")), DelYn.Y)
                        )
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N)
                        )
//                        clues.add(
//                            criteriaBuilder.isNotNull(root.get<LocalDateTime>("applyDate"))
//                        )
                    }
                    "CANCEL" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.Y)
                        )
                    }
                }
            }
            query.orderBy(criteriaBuilder.desc(root.get<LocalDateTime>("createDate")))
            criteriaBuilder.and(*clues.toTypedArray())

        }
        return spec
    }

    fun cancelInoutDiscount(inoutDiscountSn: Long): Boolean {
        try {
            inoutDiscountRepository.findBySnAndDelYn(inoutDiscountSn, DelYn.N)?.let { inoutDiscount ->
                inoutDiscount.delYn = DelYn.Y
                inoutDiscountRepository.save(inoutDiscount)
                //수량 증가
                corpTicketHistoryRepository.findBySnAndDelYn(inoutDiscount.ticketHistSn!!, DelYn.N)?.let { hist ->
                    hist.useQuantity = hist.useQuantity.minus(inoutDiscount.quantity!!)
                    corpTicketHistoryRepository.save(hist)
                    corpTicketRepository.findBySnAndDelYn(hist.ticketSn, DelYn.N)?.let { info ->
                        info.useQuantity = info.useQuantity.minus(inoutDiscount.quantity!!)
                        corpTicketRepository.save(info)
                    }
                }
            }
        }catch (e: CustomException) {
            logger.error { "cancelInoutDiscount error $e" }
            return false
        }
        return true
    }

    fun getTodayUseDiscountTicket(corpSn: Long, discountClassSn: Long) : Int {
        try {
            inoutDiscountRepository.findByCorpSnAndDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(corpSn, discountClassSn, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(DateUtil.nowDate), DelYn.N)?.let {
                return it.size
            }?.run {
                return 0
            }
            return 0
        }catch (e: CustomException) {
            logger.error { "getTodayUseDiscountTicket error $e" }
            return 0
        }
    }
}