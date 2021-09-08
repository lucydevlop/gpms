package io.glnt.gpms.service

import io.glnt.gpms.model.dto.ParkOutDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.DelYn
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
        return parkOutRepository.findTopByInSnAndDelYnOrderByOutDateDescSnDesc(sn, DelYn.N).map(parkOutMapper::toDTO)
    }

    fun findByUuid(uuid: String): ParkOut? {
        logger.debug { "Reqeust to get ParkOut by Uuid $uuid" }
        return parkOutRepository.findByUuid(uuid)
    }

    fun save(parkOutDTO: ParkOutDTO): ParkOutDTO {
        var parkOut = parkOutMapper.toEntity(parkOutDTO)
        parkOut = parkOutRepository.saveAndFlush(parkOut!!)
        return parkOutMapper.toDTO(parkOut)
    }
}