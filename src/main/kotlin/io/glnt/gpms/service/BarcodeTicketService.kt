package io.glnt.gpms.service

import io.glnt.gpms.model.dto.BarcodeClassDTO
import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.mapper.BarcodeTicketMapper
import io.glnt.gpms.model.repository.BarcodeTicketsRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BarcodeTicketService (
    private val barcodeTicketMapper: BarcodeTicketMapper,
    private val barcodeTicketsRepository: BarcodeTicketsRepository
){
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<BarcodeTicketsDTO> {
        return barcodeTicketsRepository.findAll().map(barcodeTicketMapper::toDto)
    }

    fun save(barcodeTicketsDTO: BarcodeTicketsDTO): BarcodeTicketsDTO {
        logger.debug("Request to save Barcode : $barcodeTicketsDTO")
        val barcodeTickets = barcodeTicketMapper.toEntity(barcodeTicketsDTO)
        barcodeTickets.apply {
            this.delYn = DelYn.N
        }
        barcodeTicketsRepository.save(barcodeTickets)

        return barcodeTicketMapper.toDto(barcodeTickets)
    }

}