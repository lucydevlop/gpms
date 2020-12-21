package io.glnt.gpms.handler.product.service

import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.exception.CustomException
import io.glnt.gpms.handler.product.model.reqCreateProduct
import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.repository.ProductTicketRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.lang.RuntimeException
import java.time.LocalDateTime
import javax.transaction.Transactional

@Service
class ProductService {
    companion object : KLogging()

    @Autowired
    private lateinit var productTicketRepository: ProductTicketRepository

    fun getValidProductByVehicleNo(vehicleNo: String): ProductTicket? {
        return productTicketRepository.findByVehicleNoAndValidDateGreaterThanEqualAndRegDateLessThanEqualAndFlagIsNullOrFlag(vehicleNo, LocalDateTime.now(), LocalDateTime.now(), 0)
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
            productTicketRepository.findByVehicleNoAndValidDateGreaterThanEqualAndFlagIsNullOrFlag(request.vehicleNo, request.startDate, 0
            )?.let { ticket ->
                // exists product
                // case endDate > validate -> 이력 생성
                if (request.endDate > ticket.validDate) {
                    val date = DateUtil.formatDateTime(request.startDate, "yyyyMMddHHmmss").substring(0, 8)+"235959"
                    ticket.validDate = DateUtil.getAddDays(DateUtil.stringToLocalDateTime(date), -1)
                    saveProductTicket(ticket)

                    val new = ProductTicket(
                        sn = null, vehicleNo = request.vehicleNo, flag = 0,
                        regDate = request.startDate, validDate = request.endDate,
                        userId = request.userId, gates = request.gateId!!, ticketType = request.ticktType
                    )
                    saveProductTicket(new)
                } else {
                    //gates list update
                    request.gateId!!.forEach { gate ->
                        if (!ticket.gates!!.contains(gate)) ticket.gates!!.add(gate) }
                    // case endDate =< validate -> skip(update)
                    ticket.userId = request.userId
//                    it.gates = request.gateId!!
                    ticket.ticketType = request.ticktType

                    saveProductTicket(ticket)
                }
            } ?: run {
                val new = ProductTicket(
                    sn = null,
                    vehicleNo = request.vehicleNo,
                    flag = 0,
                    regDate = request.startDate,
                    validDate = request.endDate,
                    userId = request.userId,
                    gates = request.gateId!!,
                    ticketType = request.ticktType
                )
                saveProductTicket(new)
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
}