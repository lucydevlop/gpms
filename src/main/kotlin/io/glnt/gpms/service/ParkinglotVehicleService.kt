package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.ParkinglotVehicleDTO
import io.glnt.gpms.model.mapper.ParkinglotVehicleMapper
import io.glnt.gpms.model.repository.ParkinglotVehicleRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ParkinglotVehicleService (
    private val parkinglotVehicleRepository: ParkinglotVehicleRepository,
    private val parkinglotVehicleMapper: ParkinglotVehicleMapper
){
    companion object : KLogging()

    fun save(parkinglotVehicleDTO: ParkinglotVehicleDTO) : ParkinglotVehicleDTO {
        var parkinglotVehicle = parkinglotVehicleMapper.toEntity(parkinglotVehicleDTO)
        parkinglotVehicle = parkinglotVehicleRepository.save(parkinglotVehicle!!)
        return parkinglotVehicleMapper.toDto(parkinglotVehicle)

    }
}