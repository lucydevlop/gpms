package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.ParkOutDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.mapper.ParkOutMapper
import io.glnt.gpms.model.repository.ParkOutRepository
import mu.KLogging
import org.springframework.stereotype.Service
import java.util.*

@Service
class ParkOutService (
    private val parkOutRepository: ParkOutRepository,
    private val parkOutMapper: ParkOutMapper
){
    companion object : KLogging()

    fun findOne(sn: Long): Optional<ParkOutDTO> {
        logger.debug { "Request to get ParkOut $sn" }
        return parkOutRepository.findBySn(sn).map(parkOutMapper::toDTO)
    }

    fun findByInSn(sn: Long): Optional<ParkOutDTO> {
        logger.debug { "Reqeust to get ParkOut by InSn $sn" }
        return parkOutRepository.findTopByInSnAndDelYnOrderByOutDateDescSnDesc(sn, YN.N).map(parkOutMapper::toDTO)
    }

    fun findByUuid(uuid: String): ParkOut? {
        logger.debug { "Reqeust to get ParkOut by Uuid $uuid" }
        return parkOutRepository.findByUuid(uuid)
    }

    fun findByLastVehicleNo(vehicleNo: String, gateId: String): Optional<ParkOutDTO> {
        logger.debug { "Reqeust to get Last ParkOut by vehicleNo $vehicleNo gateId $gateId" }
        return parkOutRepository.findTopByVehicleNoAndGateIdAndInSnNotOrderByOutDateDesc(vehicleNo, gateId, 0).map(parkOutMapper::toDTO)
    }

    fun save(parkOutDTO: ParkOutDTO): ParkOutDTO {
        var parkOut = parkOutMapper.toEntity(parkOutDTO)
        parkOut = parkOutRepository.saveAndFlush(parkOut!!)
        return parkOutMapper.toDTO(parkOut).apply {
                originDayDiscountFee = parkOutDTO.originDayDiscountFee
                originDiscountFee = parkOutDTO.originDiscountFee
                originParkFee = parkOutDTO.originParkFee
                originPayFee = parkOutDTO.originPayFee
                originParkTime = parkOutDTO.originParkTime

        }
    }
}