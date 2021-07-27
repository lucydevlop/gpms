package io.glnt.gpms.service

import io.glnt.gpms.model.dto.BarcodeTicketsDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.mapper.BarcodeTicketsMapper
import io.glnt.gpms.model.repository.BarcodeTicketsRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BarcodeTicketService (
    private var barcodeTicketsMapper: BarcodeTicketsMapper,
    private var barcodeTicketsRepository: BarcodeTicketsRepository
){
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<BarcodeTicketsDTO> {
        return barcodeTicketsRepository.findAll().map(barcodeTicketsMapper::toDto)
    }

    fun save(barcodeTicketsDTO: BarcodeTicketsDTO): BarcodeTicketsDTO {
        logger.debug("Request to save Barcode : $barcodeTicketsDTO")
//        barcodeTicketsDTO.delYn = DelYn.N
        val barcodeTickets = barcodeTicketsMapper.toEntity(barcodeTicketsDTO)
        barcodeTicketsRepository.save(barcodeTickets!!)

        return barcodeTicketsMapper.toDto(barcodeTickets)
    }

}