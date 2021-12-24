package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.StatisticsInoutDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.repository.InoutPaymentRepository
import org.springframework.stereotype.Service

@Service
class StatisticsInoutMapper(
    private val inoutPaymentRepository: InoutPaymentRepository
) {
    fun toDTO(entity: ParkOut) : StatisticsInoutDTO {
        val payment = inoutPaymentRepository.findByOutSnAndResultAndDelYn(entity.sn!!, ResultType.SUCCESS, YN.N)?.let { payment ->
            payment.sumBy { it.amount!! }
        }?: kotlin.run { 0 }

        return StatisticsInoutDTO(
            inSn = entity.inSn,
            vehicleNo = entity.vehicleNo,
            outSn = entity.sn,
            parkFee = entity.parkfee?: 0,
            discountFee = entity.discountfee?: 0,
            dayDiscountFee = entity.dayDiscountfee?: 0,
            payFee = entity.payfee?: 0,
            payment = payment,
            nonPayment = entity.payfee?.minus(payment)
        )
    }
}