package io.glnt.gpms.handler.product.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.product.model.reqCreateProduct
import io.glnt.gpms.handler.product.model.reqSearchProduct
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.repository.ProductTicketRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.time.LocalDateTime
import javax.persistence.criteria.Predicate
import javax.transaction.Transactional

@Service
class ProductService {
    companion object : KLogging()

    @Autowired
    private lateinit var productTicketRepository: ProductTicketRepository

    fun getValidProductByVehicleNo(vehicleNo: String): ProductTicket? {
        return productTicketRepository.findByVehicleNoAndValidDateGreaterThanEqualAndRegDateLessThanEqualAndDelYn(vehicleNo, LocalDateTime.now(), LocalDateTime.now(), DelYn.N)
    }

    fun getValidProductByVehicleNo(vehicleNo: String, startTime: LocalDateTime, endTime: LocalDateTime): ProductTicket? {
        return productTicketRepository.findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(vehicleNo, startTime, endTime, DelYn.N)
    }

    fun calcRemainDayProduct(vehicleNo: String): Int {
        getValidProductByVehicleNo(vehicleNo)?.let { it ->
            return DateUtil.diffDays(LocalDateTime.now(), it.validDate!!)
        }
        return -1
    }

    @Throws(CustomException::class)
    fun createProduct(request: reqCreateProduct): Boolean {
        logger.info { "createProduct request $request" }
        try {
            if (request.sn != null) {
                productTicketRepository.findBySn(request.sn!!)?.let {
                    val new = ProductTicket(
                        sn = it.sn,
                        vehicleNo = request.vehicleNo,
                        delYn = DelYn.N,
                        effectDate = request.effectDate,
                        expireDate = request.expireDate,
                        userId = request.userId?.let { request.userId } ?: run { it.userId },
                        gates = request.gateId?.let { request.gateId } ?: run { it.gates },
                        ticketType = request.ticketType?.let { request.ticketType } ?: run { it.ticketType },
                        vehicleType = request.vehicleType?.let { request.vehicleType } ?: run { it.vehicleType },
                        corpSn = request.corpSn?.let { request.corpSn } ?: run { it.corpSn }
                    )
                    saveProductTicket(new)
                } ?: run {
                    return false
                }
            } else {
                productTicketRepository.findByVehicleNoAndValidDateGreaterThanEqualAndDelYn(request.vehicleNo, request.effectDate, DelYn.N
                )?.let { ticket ->
                    // exists product
                    // case endDate > validate -> 이력 생성
                    if (request.expireDate > ticket.expireDate) {
                        val date = DateUtil.formatDateTime(request.effectDate, "yyyyMMddHHmmss").substring(0, 8)+"235959"
                        ticket.expireDate = DateUtil.getAddDays(DateUtil.stringToLocalDateTime(date), -1)
                        saveProductTicket(ticket)

                        val new = ProductTicket(
                            sn = null, vehicleNo = request.vehicleNo, delYn = DelYn.N,
                            effectDate = request.effectDate, expireDate = request.expireDate,
                            userId = request.userId, gates = request.gateId!!, ticketType = request.ticketType,
                            vehicleType = request.vehicleType, corpSn = request.corpSn
                        )
                        saveProductTicket(new)
                    } else {
                        //gates list update
                        request.gateId!!.forEach { gate ->
                            if (!ticket.gates!!.contains(gate)) ticket.gates!!.add(gate) }
                        // case endDate =< validate -> skip(update)
                        ticket.userId = request.userId
//                    it.gates = request.gateId!!
                        ticket.ticketType = request.ticketType

                        saveProductTicket(ticket)
                    }
                } ?: run {
                    val new = ProductTicket(
                        sn = null,
                        vehicleNo = request.vehicleNo,
                        delYn = DelYn.N,
                        effectDate = request.effectDate,
                        expireDate = request.expireDate,
                        userId = request.userId,
                        gates = request.gateId,
                        ticketType = request.ticketType,
                        vehicleType = request.vehicleType,
                        corpSn = request.corpSn
                    )
                    saveProductTicket(new)
                }
            }
        } catch (e: RuntimeException) {
            logger.info { "createProduct error ${e.message}" }
            return false
        }
        return true
    }

    @Transactional
    fun saveProductTicket(data: ProductTicket) : ProductTicket {
        return productTicketRepository.save(data)
    }

    fun getProducts(request: reqSearchProduct): CommonResult {
        return CommonResult.data(productTicketRepository.findAll(findAllProductSpecification(request)))
    }

    fun deleteTicket(request: Long) : CommonResult {
        logger.info { "delete ticket : $request" }
        try {
            productTicketRepository.findBySn(request)?.let { ticket ->
                ticket.delYn = DelYn.Y
                return CommonResult.data(productTicketRepository.save(ticket))
            }
        }catch (e: CustomException) {
            logger.error { "deleteTicket error ${e.message}" }
        }
        return CommonResult.error("deleteTicket failed")
    }

    private fun findAllProductSpecification(request: reqSearchProduct): Specification<ProductTicket> {
        val spec = Specification<ProductTicket> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.searchLabel == "CARNUM" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                )
            }
            if (request.from != null && request.to != null) {
                clues.add(
                    criteriaBuilder.between(
                        root.get("validDate"),
                        DateUtil.beginTimeToLocalDateTime(request.from.toString()),
                        DateUtil.lastTimeToLocalDateTime(request.to.toString())
                    )
                )
            }
            if (request.ticketType != null) {
                clues.add(
                    criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("ticketType")), request.ticketType)
                )
            }
            if (request.delYn != "ALL") {
                when(request.delYn) {
                    "Y" -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.Y)
                        )
                    }
                    else -> {
                        clues.add(
                            criteriaBuilder.equal(criteriaBuilder.upper(root.get<String>("delYn")), DelYn.N)
                        )
                    }
                }
            }

            criteriaBuilder.and(*clues.toTypedArray())
        }
        return spec
    }


}