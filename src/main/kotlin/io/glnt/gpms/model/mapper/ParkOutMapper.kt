package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.entity.GateDTO
import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.dto.entity.ParkOutDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.repository.GateRepository
import io.glnt.gpms.model.repository.ParkInRepository
import org.springframework.stereotype.Service

@Service
class ParkOutMapper (
    private val gateRepository: GateRepository,
    private val parkInRepository: ParkInRepository
) {
    fun toDTO(entity: ParkOut): ParkOutDTO {
        ParkOutDTO(entity).apply {
            gateRepository.findByGateId(this.gateId ?: "")?.let {
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