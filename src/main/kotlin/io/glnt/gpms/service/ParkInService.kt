package io.glnt.gpms.service

import io.glnt.gpms.model.dto.ParkInDTO
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.stereotype.Service

@Service
class ParkInService(
    private val parkInRepository: ParkInRepository,
    private val parkInMapper: ParkInMapper
) {
    companion object : KLogging()

    fun save(parkInDTO: ParkInDTO) : ParkInDTO {
        var parkIn = parkInMapper.toEntity(parkInDTO)
        parkIn = parkInRepository.saveAndFlush(parkIn!!)
        return parkInMapper.toDTO(parkIn)
    }

}