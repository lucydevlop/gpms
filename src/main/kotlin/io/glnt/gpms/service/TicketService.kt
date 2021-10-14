package io.glnt.gpms.service

import io.glnt.gpms.model.dto.ProductTicketDTO
import io.glnt.gpms.model.mapper.ProductTicketMapper
import io.glnt.gpms.model.repository.ProductTicketRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class TicketService(
    private val productTicketMapper: ProductTicketMapper,
    private val productTicketRepository: ProductTicketRepository,
    private val corpService: CorpService
) {
    companion object : KLogging()

    fun saveTickets(ticketDTOs: ArrayList<ProductTicketDTO>): List<ProductTicketDTO> {
        logger.debug { "Request to save Tickets : $ticketDTOs" }
        val list = ArrayList<ProductTicketDTO>()
        ticketDTOs.forEach { productTicketDTO ->
            productTicketDTO.corpName?.let {corpName ->
                corpService.getStoreByCorpName(corpName).ifPresent { corp ->
                    productTicketDTO.corpSn = corp.sn
                }
            }
            val ticket = save(productTicketDTO)
            list.add(ticket)
        }
        return list
    }

    fun save(productTicketDTO: ProductTicketDTO): ProductTicketDTO {
        logger.debug { "Request to save Ticket : $productTicketDTO" }
        val ticket = productTicketMapper.toEntity(productTicketDTO)
        productTicketRepository.save(ticket!!)
        return productTicketMapper.toDTO(ticket)
    }
}