package io.glnt.gpms.model.mapper

import io.glnt.gpms.handler.tmap.model.parkinglotMap
import io.glnt.gpms.model.dto.GateDTO
import io.glnt.gpms.model.dto.ParkInDTO
import io.glnt.gpms.model.dto.ParkOutDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.repository.ParkInRepository
import io.glnt.gpms.service.ParkInQueryService
import org.springframework.stereotype.Service

@Service
class ParkOutMapper (
    private val gateRepository: GateRepository,
    private val parkInRepository: ParkInRepository
) {
    fun toDTO(entity: ParkOut): ParkOutDTO {
        ParkOutDTO(entity).apply {
            gateRepository.findByGateId(this.gateId ?: "").ifPresent {
                this.gate = GateDTO(it)
            }
            this.inSn?.let { it ->
                this.parkInDTO = parkInRepository.findBySn(it)?.let { it1 -> ParkInDTO(it1) }
            }
            return this
        }
    }

    fun toEntity(dto: ParkOutDTO) =
        when(dto) {
            null -> null
            else -> {
                ParkOut(
                    sn = dto.sn,
                    gateId = dto.gateId,
                    parkcartype = dto.parkcartype,
                    vehicleNo = dto.vehicleNo,
                    outDate = dto.outDate,
                    date = dto.date,
                    image = dto.image,
                    resultcode = dto.resultcode,
                    inSn = dto.inSn,
                    requestid = dto.requestid,
                    uuid = dto.uuid,
                    parktime = dto.parktime,
                    parkfee = dto.parkfee,
                    payfee = dto.payfee,
                    discountfee = dto.discountfee,
                    dayDiscountfee = dto.dayDiscountfee,
                    fileuploadid = dto.fileuploadid,
                    delYn = dto.delYn
                )
            }
        }
}