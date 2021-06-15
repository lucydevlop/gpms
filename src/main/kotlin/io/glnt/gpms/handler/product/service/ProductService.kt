package io.glnt.gpms.handler.product.service

import io.glnt.gpms.common.api.CommonResult
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.product.model.reqSearchProduct
import io.glnt.gpms.model.dto.request.reqCreateProductTicket
import io.glnt.gpms.model.dto.request.reqSearchProductTicket
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.entity.TicketClass
import io.glnt.gpms.model.enums.DateType
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.repository.ProductTicketRepository
import io.glnt.gpms.model.repository.TicketClassRepository
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

    @Autowired
    private lateinit var ticketClassRepository: TicketClassRepository

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
    fun createProduct(request: reqCreateProductTicket): CommonResult {
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
                        corpSn = request.corpSn?.let { request.corpSn } ?: run { it.corpSn },
                        etc = request.etc?.let { request.etc } ?: run { it.etc },
                        etc1 = request.etc1?.let { request.etc1 } ?: run { it.etc1 },
                        name = request.name?.let { request.name } ?: run { it.name },
                        tel = request.tel?.let { request.tel } ?: run { it.tel },
                        vehiclekind = request.vehiclekind?.let { request.vehiclekind } ?: run { it.vehiclekind },
                        ticketSn = request.ticketSn?.let { request.ticketSn } ?: run { it.ticketSn },

                    )
                    return CommonResult.data(saveProductTicket(new))
                } ?: run {
                    return CommonResult.error("product ticket create failed")
                }
            } else {
                //동일 차량 등록 시 skip
                productTicketRepository.findByVehicleNoAndEffectDateAndExpireDateAndTicketTypeAndDelYn(request.vehicleNo, request.effectDate, request.expireDate, request.ticketType!!, DelYn.N)?.let {
                    if (it.isNotEmpty()) return CommonResult.data("product ticket exists")
                }

//                productTicketRepository.findByVehicleNoAndExpireDateGreaterThanEqualAndEffectDateLessThanEqualAndDelYn(request.vehicleNo, request.expireDate, request.effectDate, DelYn.N
                productTicketRepository.findByVehicleNoAndValidDateGreaterThanEqualAndDelYn(request.vehicleNo, request.effectDate, DelYn.N
                )?.let { ticket ->
                    // exists product
                    // case db 이력 range 안에 신규 이력 range 존재
//                    ticket.expireDate = DateUtil.getAddDays(DateUtil.stringToLocalDateTime(date), -1)
//                    saveProductTicket()


                    // case endDate > validate -> 이력 생성
                    if (request.expireDate > ticket.expireDate) {
                        val date = DateUtil.formatDateTime(request.effectDate, "yyyyMMddHHmmss").substring(0, 8)+"235959"
                        ticket.expireDate = DateUtil.getAddDays(DateUtil.stringToLocalDateTime(date), -1)
                        saveProductTicket(ticket)

                        val new = ProductTicket(
                            sn = null, vehicleNo = request.vehicleNo, delYn = DelYn.N,
                            effectDate = request.effectDate, expireDate = request.expireDate,
                            userId = request.userId, gates = request.gateId!!, ticketType = request.ticketType,
                            vehicleType = request.vehicleType, corpSn = request.corpSn, etc = request.etc,
                            name = request.name, etc1 = request.etc1, tel = request.tel, vehiclekind = request.vehiclekind, ticketSn = request.ticketSn
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
                        corpSn = request.corpSn,
                        etc = request.etc,
                        name = request.name , etc1 = request.etc1, tel = request.tel, vehiclekind = request.vehiclekind,
                        ticketSn = request.ticketSn
                    )
                    return CommonResult.data(saveProductTicket(new))
                }
            }
        } catch (e: RuntimeException) {
            logger.info { "createProduct error $e" }
            return CommonResult.error("product ticket create failed")
        }
        return CommonResult.data()
    }

    @Transactional
    fun saveProductTicket(data: ProductTicket) : ProductTicket {
        return productTicketRepository.saveAndFlush(data)
    }

    fun getProducts(request: reqSearchProductTicket): List<ProductTicket>? {
        return productTicketRepository.findAll(findAllProductSpecification(request))
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

    private fun findAllProductSpecification(request: reqSearchProductTicket): Specification<ProductTicket> {
        val spec = Specification<ProductTicket> { root, query, criteriaBuilder ->
            val clues = mutableListOf<Predicate>()

            if(request.searchLabel == "CARNUM" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("vehicleNo")), likeValue)
                )
            }
            if(request.searchLabel == "USERNAME" && request.searchText != null) {
                val likeValue = "%" + request.searchText + "%"
                clues.add(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get<String>("name")), likeValue)
                )
            }
            if (request.searchDateLabel == DateType.EFFECT) {
                if (request.fromDate != null && request.toDate != null) {
                    clues.add(
                        criteriaBuilder.between(
                            root.get("effectDate"),
                            DateUtil.beginTimeToLocalDateTime(request.fromDate.toString()),
                            DateUtil.lastTimeToLocalDateTime(request.toDate.toString())
                        )
                    )
                }
            }
            if (request.searchDateLabel == DateType.EXPIRE) {
                if (request.fromDate != null && request.toDate != null) {
                    clues.add(
                        criteriaBuilder.between(
                            root.get("expireDate"),
                            DateUtil.beginTimeToLocalDateTime(request.fromDate.toString()),
                            DateUtil.lastTimeToLocalDateTime(request.toDate.toString())
                        )
                    )
                }
            }

            if (request.searchDateLabel == DateType.VALIDATE) {
                if (request.fromDate != null && request.toDate != null) {
                    clues.add(
                        criteriaBuilder.lessThanOrEqualTo(
                            root.get("effectDate"),
                            DateUtil.lastTimeToLocalDateTime(request.toDate.toString())
                        )
                    )
                    clues.add(
                        criteriaBuilder.greaterThanOrEqualTo(
                            root.get("expireDate"),
                            DateUtil.lastTimeToLocalDateTime(request.fromDate.toString())
                        )
                    )
                }
            }

            if (request.effectDate != null) {
                clues.add(
                    criteriaBuilder.equal(root.get<String>("effectDate"), request.effectDate)
                )
            }

            if (request.expireDate != null) {
                clues.add(
                    criteriaBuilder.equal(root.get<String>("expireDate"), request.expireDate)
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

    fun getTicketClass() : List<TicketClass>? {
        return ticketClassRepository.findAll()
    }

    fun createTicketClass(request: TicketClass): TicketClass? {
        try {
            ticketClassRepository.findByTicketNameAndDelYn(request.ticketName, DelYn.N)?.let {
                return null
            }?: kotlin.run {
                return ticketClassRepository.save(request)
            }
        }catch (e: RuntimeException) {
            logger.info { "createTicketClass error $e" }
            return null
        }
    }
}