package io.glnt.gpms.service

import io.glnt.gpms.model.mapper.BarcodeClassMapper
import io.glnt.gpms.model.dto.entity.BarcodeClassDTO
import io.glnt.gpms.model.entity.BarcodeClass
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.repository.BarcodeClassRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.persistence.EntityNotFoundException

@Service
class BarcodeClassService(
    private val barcodeClassMapper: BarcodeClassMapper,
    private val barcodeClassRepository: BarcodeClassRepository
) {
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<BarcodeClassDTO> {
        return barcodeClassRepository.findAll().map(barcodeClassMapper::toDto)
    }

    fun save(barcodeClassDTO: BarcodeClassDTO): BarcodeClassDTO {
        logger.debug("Request to save BarcodeClass : $barcodeClassDTO")
        val barcodeClass = barcodeClassMapper.toEntity(barcodeClassDTO)?.let {
            barcodeClassRepository.save(it)
        }

        return barcodeClassMapper.toDto(barcodeClass!!)
    }

    fun findByStartLessThanEqualAndEndGreaterThanAndDelYn(price: Int): BarcodeClass? {
        return barcodeClassRepository.findByStartLessThanEqualAndEndGreaterThanAndDelYn(price, price, YN.N)
    }

    fun delete(sn: Long): BarcodeClassDTO {
        logger.debug("Request to delete BarcodeClass : $sn")
        val barcodeClass = barcodeClassRepository.findBySn(sn).orElseThrow { EntityNotFoundException("BarcodeClass sn") }
        barcodeClass.delYn = YN.Y
        barcodeClassRepository.save(barcodeClass)

        return barcodeClassMapper.toDto(barcodeClass)
    }
}