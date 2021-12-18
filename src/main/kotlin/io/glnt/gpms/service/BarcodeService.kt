package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.BarcodeDTO
import io.glnt.gpms.model.repository.BarcodeRepository
import io.glnt.gpms.model.mapper.BarcodeMapper
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class BarcodeService (
    private val barcodeRepository: BarcodeRepository,
    private val barcodeMapper: BarcodeMapper
){
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<BarcodeDTO> {
        return barcodeRepository.findAll().map(barcodeMapper::toDto)
    }

    fun save(barcodeDTO: BarcodeDTO): BarcodeDTO {
        logger.debug("Request to save Barcode : $barcodeDTO")
        val barcode = barcodeMapper.toEntity(barcodeDTO)
        barcodeRepository.save(barcode!!)
        return barcodeMapper.toDto(barcode)
    }

}