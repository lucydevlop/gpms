package io.glnt.gpms.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.dashboard.admin.model.reqCreateCorpTicket
import io.glnt.gpms.handler.dashboard.common.model.reqParkingDiscountSearchTicket
import io.glnt.gpms.handler.discount.model.*
import io.glnt.gpms.model.dto.entity.DiscountClassDTO
import io.glnt.gpms.model.dto.request.ReqAddParkingDiscount
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.*
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate

@Service
class DiscountService(
    private val discountClassService: DiscountClassService,
    private val inoutDiscountService: InoutDiscountService,
    private val corpService: CorpService
) {
    companion object : KLogging()

    @Autowired
    private lateinit var corpTicketRepository: CorpTicketRepository

    @Autowired
    private lateinit var corpTicketHistoryRepository: CorpTicketHistoryRepository

    @Autowired
    private lateinit var inoutDiscountRepository: InoutDiscountRepository

    @Autowired
    private lateinit var corpTicketClassRepository: CorpTicketClassRepository

    fun deleteDiscountClass(sn: Long) : DiscountClassDTO {
        logger.info { "deleteDiscountClass $sn" }
        val discountClass = discountClassService.findBySn(sn)
        discountClass.delYn = YN.Y
        return discountClassService.save(discountClass)
    }

    fun searchCorpTicketByCorpAndDiscountClass(corpSn: Long, discountClassNo: Long): CommonResult {
        try{
            return CommonResult.data(corpTicketRepository.findByCorpSnAndClassSnAndDelYn(corpSn, discountClassNo, YN.N))

        }catch (e: CustomException){
            logger.error { "searchCorpTicketByCorpAndDiscountClass error $e" }
            return CommonResult.error("searchCorpTicketByCorpAndDiscountClass search failed")
        }
    }

    fun createDiscountClass(request: DiscountClass): DiscountClassDTO? {
        logger.info { "createDiscountClass $request" }
        try {
            return discountClassService.save(DiscountClassDTO(request))
        }catch (e: RuntimeException) {
            logger.error { "createDiscountClass error $e" }
            return null
        }
    }

    @Throws(CustomException::class)
    fun createCorpTicket(request: reqCreateCorpTicket): CommonResult {
        try {
            val corpTicket = corpTicketRepository.findByCorpSnAndClassSnAndDelYn(request.corpSn, request.classSn, YN.N)?.let {
                it.totalQuantity = it.totalQuantity.plus(request.quantity!!)
                corpTicketRepository.save(it)
            } ?: run {
                corpTicketRepository.save(
                    CorpTicketInfo(sn = null, corpSn = request.corpSn, classSn = request.classSn,
                        totalQuantity = request.quantity!!, useQuantity = 0, delYn = YN.N)
                )
            }

            corpTicketHistoryRepository.save(
                CorpTicketHistory(sn = null, ticketSn = corpTicket.sn!!, totalQuantity = request.quantity!!,
                    effectDate = LocalDateTime.now(), delYn = YN.N)
            )
            return CommonResult.data()
        }catch (e: CustomException){
            logger.error { "createCorpTicket error $e" }
            return CommonResult.error("createCorpTicket failed")
        }
    }

    @Throws(CustomException::class)
    fun deleteCorpTicket(sn: Long) : CorpTicketInfo? {
        try {
            return corpTicketRepository.findBySnAndDelYn(sn, YN.N)?.let {
                it.delYn = YN.Y
                corpTicketRepository.saveAndFlush(it)
            }?: kotlin.run {
                return null
            }
        }catch (e: CustomException){
            logger.error { "deleteCorpTicket error $e" }
            return null
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

    fun searchCorpTicketByCorp(request: reqParkingDiscountSearchTicket) : CommonResult {
        try{
            val result = ArrayList<Any>()
            request.searchLabel?.let {
                val data = corpService.getStoreBySn(request.searchText!!.toLong())
                data.ifPresent { corp ->
                    val tickets = corpTicketRepository.findByCorpSnAndDelYn(corp.sn!!, YN.N)
                    if (tickets!!.isNotEmpty()) {
                        tickets.forEach {
                            result.add(it)
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


//    fun getDiscountableTickets(request: reqDiscountableTicket): CommonResult {
//        logger.info { "getDiscountableTickets $request" }
//        try {
//            val tickets = searchCorpTicketByCorp(reqParkingDiscountSearchTicket(searchLabel = "CORPSN", searchText=request.corpSn.toString()))
//            val weekRange = DateUtil.getWeekRange(DateUtil.formatDateTime(request.date!!, "yyyy-MM-dd"))
//
//            if (tickets.code == ResultCode.SUCCESS.getCode() && tickets.data != null) {
//                val data = ArrayList<CorpTicketInfo>()
//
//
//                for (ticket in tickets.data as List<CorpTicketInfo>) {
//                    corpTicketHistoryRepository.findByTicketSnAndDelYn(ticket.sn!!, DelYn.N)?.let { hist ->
//                        ticket.ableCnt = getAbleDiscountCnt(reqSearchInoutDiscount(ticketSn = ticket.discountClass!!.sn!!, inSn = request.inSn!!))
//                        if (ticket.ableCnt != null) {
//                            if (ticket.ableCnt!! > 0 ) data.add(ticket)
//                        }
//                    }
//
//                }
//
//                val result = data.filter {
//                    // 입차 요일 확인
//                    ( it.discountClass!!.dayRange == weekRange || it.discountClass!!.dayRange == DiscountRangeType.ALL)
//                }
//                if (result.isNullOrEmpty()) return CommonResult.data()
//                return CommonResult.data(result)
//            }
//            return CommonResult.data()
//        }catch (e: CustomException){
//            logger.error { "getDiscountableTickets error ${e.message}" }
//            return CommonResult.error("getDiscountableTickets search failed")
//        }
//    }

    fun getAbleDiscountCnt(request: reqSearchInoutDiscount) : Long? {
        logger.info { "getAbleDiscountCnt $request" }
        try {
            val result = ArrayList<Long>()
            corpTicketClassRepository.findBySn(request.ticketSn).let { corpTicketClass ->
//                discountClassRepository.findBySn(corpTicketClass.discountClassSn!!).let { discountClass ->
                inoutDiscountRepository.findByInSnAndDiscountClassSnAndDelYn(request.inSn, corpTicketClass.discountClassSn!!, YN.N)?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > corpTicketClass.onceMax!!) return 0
                    if (it == 0) result.add(corpTicketClass.onceMax!!) else  result.add(corpTicketClass.onceMax!!-it)
                }
                inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
                    request.ticketSn, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(DateUtil.nowDate), YN.N )?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > corpTicketClass.dayMax!!) return 0
                    if (it == 0) result.add(corpTicketClass.dayMax!!) else  result.add(corpTicketClass.dayMax!!-it)
                }
                inoutDiscountRepository.findByDiscountClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(
                    request.ticketSn, DateUtil.firstDayToLocalDateTime(DateUtil.nowDate), DateUtil.lastDayToLocalDateTime(DateUtil.nowDate), YN.N )?.sumBy { it -> it.quantity!! }.also {
                    if (it!! > corpTicketClass.monthMax!!) return 0
                    if (it == 0) result.add(corpTicketClass.monthMax!!) else  result.add(corpTicketClass.monthMax!!-it)
                }
//                }
            }
            return result.minOrNull()
        }catch (e: CustomException){
            logger.error { "getInoutDiscount error ${e.message}" }
            return null//CommonResult.error("getInoutDiscount search failed")
        }
    }

    fun addInoutDiscount(request: reqAddInoutDiscount): InoutDiscount {
        return inoutDiscountRepository.save(
            InoutDiscount(sn = null, discontType = request.discountType, discountClassSn = request.discountClassSn,
                          ticketHistSn = request.ticketSn, inSn = request.inSn, quantity = request.quantity, delYn = YN.N,
                          corpSn = request.corpSn, ticketClassSn = request.ticketClassSn))
    }

    fun saveInoutDiscount(discount: InoutDiscount) : InoutDiscount {
        return inoutDiscountRepository.saveAndFlush(discount)
    }

    fun applyInoutDiscount(inSn: Long) {
        try {
            inoutDiscountRepository.findByInSnAndDelYnAndCalcYn(inSn, YN.N, YN.Y)?.let { discounts ->
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
        return inoutDiscountRepository.findByInSnAndDelYn(inSn, YN.N)
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

            if(request.ticketClassSn != null){
                clues.add(
                    // criteriaBuilder.equal(root.get<Long>("discountClassSn"), request.discountClassSn)
                    criteriaBuilder.equal(criteriaBuilder.lower(root.get<String>("ticketClassSn")),request.ticketClassSn)
                )
            }


//            if (request.ticketType != TicketType.ALL) {
//                clues.add(
//                    criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("ticketType")), request.ticketType.code)
//                )
//            }
            if (request.applyStatus != null) {
                when (request.applyStatus) {
                    "DO" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("calcYn")), YN.N)
                        )
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), YN.N)
                        )
                    }
                    "DONE" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("calcYn")), YN.Y)
                        )
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), YN.N)
                        )
//                        clues.add(
//                            criteriaBuilder.isNotNull(root.get<LocalDateTime>("applyDate"))
//                        )
                    }
                    "CANCEL" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), YN.Y)
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
            inoutDiscountRepository.findBySnAndDelYn(inoutDiscountSn, YN.N)?.let { inoutDiscount ->
                inoutDiscount.delYn = YN.Y
                inoutDiscountRepository.save(inoutDiscount)
                //수량 증가
                corpTicketHistoryRepository.findBySnAndDelYn(inoutDiscount.ticketHistSn!!, YN.N)?.let { hist ->
                    hist.useQuantity = hist.useQuantity.minus(inoutDiscount.quantity!!)
                    corpTicketHistoryRepository.save(hist)
                    corpTicketRepository.findBySnAndDelYn(hist.ticketSn!!, YN.N)?.let { info ->
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

    fun getTodayUseDiscountTicket(corpSn: Long, ticketClassSn: Long) : Int {
        try {
            inoutDiscountRepository.findByCorpSnAndTicketClassSnAndCreateDateGreaterThanEqualAndCreateDateLessThanEqualAndDelYn(corpSn, ticketClassSn, DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate), DateUtil.lastTimeToLocalDateTime(DateUtil.nowDate), YN.N)?.let { inoutDiscounts ->
                return inoutDiscounts.sumOf { it.quantity ?: 0 }
            }?.run {
                return 0
            }
            return 0
        }catch (e: CustomException) {
            logger.error { "getTodayUseDiscountTicket error $e" }
            return 0
        }
    }

    fun addInoutDiscount(request: ArrayList<ReqAddParkingDiscount>) : Boolean{
        return try {
            request.filter { it.cnt > 0 }.forEach { addDiscount ->
                saveInoutDiscount(
                    InoutDiscount(sn = null, discontType = TicketType.DISCOUNT, discountClassSn = addDiscount.discountClassSn,
                        inSn = addDiscount.inSn, quantity = addDiscount.cnt, delYn = YN.N, calcYn = YN.Y, )
                )
            }
            true
        } catch (e: CustomException) {
            logger.error { "addInoutDiscount failed $e" }
            false
        }
    }

    fun calcDiscountPercent(inSn: Long, type: CalcType, totalPrice: Int, discounts: ArrayList<ReqAddParkingDiscount>? = null): Int {
        var result = 0

        val discountClasses = ArrayList<DiscountClassDTO>()

        discounts?.let { list ->
            list.forEach { discount ->
                discountClassService.findBySn(discount.discountClassSn).apply {
                    if (this.discountApplyType == DiscountApplyType.PERCENT) {
                        discountClasses.add(this)

                        if (type == CalcType.SETTLE) {
                            inoutDiscountService.findBySn(discount.sn ?: 0)?.let {
                                inoutDiscountService.completeCalc(it)
                            }
                        }
                    }
                }
            }
            if (discountClasses.isEmpty()) return 0

            val apply =  discountClasses.maxOf { it.unitTime }
            result = ( totalPrice * apply ) / 100
        }
        return result
    }

    fun calcDiscountWonByFix(inSn: Long, type: CalcType, totalPrice: Int, discounts: ArrayList<ReqAddParkingDiscount>? = null): Int {
        var result = 0

        val discountClasses = ArrayList<DiscountClassDTO>()

        discounts?.let { list ->
            list.forEach { discount ->
                discountClassService.findBySn(discount.discountClassSn).apply {
                    if (this.discountApplyType == DiscountApplyType.WON && this.discountApplyRate == DiscountApplyRateType.FIX) {
                        discountClasses.add(this)

                        if (type == CalcType.SETTLE) {
                            inoutDiscountService.findBySn(discount.sn ?: 0)?.let {
                                inoutDiscountService.completeCalc(it)
                            }
                        }
                    }
                }
            }
            if (discountClasses.isEmpty()) return 0

            val apply =  discountClasses.minOf { it.unitTime }
            result = totalPrice - apply
        }
        return result
    }

    fun calcDiscountWonByVariable(inSn: Long, type: CalcType, totalPrice: Int, discounts: ArrayList<ReqAddParkingDiscount>? = null): Int {
        var result = 0

        val discountClasses = ArrayList<DiscountClassDTO>()

        discounts?.let { list ->
            list.forEach { discount ->
                discountClassService.findBySn(discount.discountClassSn).apply {
                    if (this.discountApplyType == DiscountApplyType.WON && this.discountApplyRate == DiscountApplyRateType.VARIABLE) {
                        this.useCnt = discount.cnt
                        discountClasses.add(this)

                        if (type == CalcType.SETTLE) {
                            inoutDiscountService.findBySn(discount.sn ?: 0)?.let {
                                inoutDiscountService.completeCalc(it)
                            }
                        }
                    }
                }
            }
            if (discountClasses.isEmpty()) return 0

            result = discountClasses.sumOf { it.unitTime * (it.useCnt?: 0) }

            //result = apply

            //result = if (apply > totalPrice) totalPrice else apply
        }
        return result
    }


}