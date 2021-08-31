package io.glnt.gpms.service

import io.glnt.gpms.model.dto.DiscountClassDTO
import io.glnt.gpms.model.dto.request.reqDiscountTicket
import io.glnt.gpms.model.entity.DiscountClass
import io.glnt.gpms.model.entity.timeRange
import io.glnt.gpms.model.mapper.DiscountClassMapper
import io.glnt.gpms.model.repository.DiscountClassRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DiscountClassService (
    private val discountClassRepository: DiscountClassRepository,
    private val discountClassMapper: DiscountClassMapper
){
    @Transactional(readOnly = true)
    fun findAll(): List<DiscountClassDTO> {
        return discountClassRepository.findAll().map(discountClassMapper::toDto)
    }

    fun save(discountClassDTO: DiscountClassDTO): DiscountClassDTO {
        var discountClass = discountClassMapper.toEntity(discountClassDTO)
        discountClass = discountClassRepository.save(discountClass!!)
        return discountClassMapper.toDto(discountClass)
    }
}