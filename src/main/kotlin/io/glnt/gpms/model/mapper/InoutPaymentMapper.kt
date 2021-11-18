package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.InoutPaymentDTO
import io.glnt.gpms.model.dto.ParkInDTO
import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.repository.InoutPaymentRepository
import io.glnt.gpms.model.repository.ParkInRepository
import org.springframework.stereotype.Service

@Service
class InoutPaymentMapper (
    private val inoutPaymentRepository: InoutPaymentRepository,
    private val parkInRepository: ParkInRepository
){
    fun toDTO(entity: InoutPayment): InoutPaymentDTO {
        InoutPaymentDTO(entity).apply {
            this.parkInDTO = parkInRepository.findBySn(this.inSn!!)?.let { ParkInDTO(it) }
            return this
        }
    }

    fun toEntity(dto: InoutPaymentDTO) =
        when(dto) {
            null -> null
            else -> {
                InoutPayment(
                    sn = dto.sn,
                    type = dto.type,
                    inSn = dto.inSn,
                    approveDateTime = dto.approveDateTime,
                    payType = dto.payType,
                    amount = dto.amount,
                    cardCorp = dto.cardCorp,
                    cardNumber = dto.cardNumber,
                    transactionId = dto.transactionId,
                    result = dto.result ?: ResultType.FAILURE,
                    failureMessage = dto.failureMessage,
                    delYn = dto.delYn,
                    outSn = dto.outSn,
                    parkTime = dto.parkTime,
                    parkFee = dto.parkFee,
                    discount = dto.discount,
                    dayDiscount = dto.dayDiscount,
                    vehicleNo = dto.vehicleNo
                )
            }
        }
}