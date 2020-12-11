package io.glnt.gpms.handler.product.service

import io.glnt.gpms.model.entity.ProductTicket
import io.glnt.gpms.model.repository.ProductTicketRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ProductService {
    companion object : KLogging()

    @Autowired
    private lateinit var productTicketRepository: ProductTicketRepository

    fun getValidProductByVehicleNo(vehicleNo: String): ProductTicket? {
        return productTicketRepository.findByVehicleNoAndValidDateGreaterThanEqualAndRegDateLessThanEqualAndFlagIsNullOrFlag(vehicleNo, LocalDateTime.now(), LocalDateTime.now(), 0)
    }

    fun getValidProduct() {

    }

}