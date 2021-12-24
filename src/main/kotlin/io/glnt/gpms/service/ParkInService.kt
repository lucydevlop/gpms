package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.mapper.ParkInMapper
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.time.LocalDateTime

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

    fun getLastVehicleNoByDate(vehicleNo: String, date: LocalDateTime): ParkIn? {
        return parkInRepository.findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo, 0L, YN.N, date)
    }

    fun findOne(sn: Long) : ParkInDTO? {
        logger.debug { "Request to get ParkIn $sn" }
        return parkInRepository.findBySn(sn)?.let { parkIn -> parkInMapper.toDTO(parkIn) }
    }


}