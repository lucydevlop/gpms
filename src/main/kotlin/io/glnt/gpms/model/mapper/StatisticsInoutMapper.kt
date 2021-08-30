package io.glnt.gpms.model.mapper

import io.glnt.gpms.model.dto.StatisticsInoutDTO
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.repository.InoutPaymentRepository
import io.glnt.gpms.model.repository.ParkOutRepository
import org.springframework.stereotype.Service

@Service
class StatisticsInoutMapper(
    private val parkOutRepository: ParkOutRepository,
    private val inoutPaymentRepository: InoutPaymentRepository
) {
    fun toDTO(entity: ParkIn) : StatisticsInoutDTO {
        val out = parkOutRepository.findTopByInSnAndDelYnOrderByOutDateDesc(entity.sn!!, DelYn.N)
        val payment = inoutPaymentRepository.findByInSnAndResultAndDelYn(entity.sn!!, ResultType.SUCCESS, DelYn.N)?.let { payment ->
            payment.sumBy { it.amount!! }
        }?: kotlin.run { 0 }

        return StatisticsInoutDTO(
            parkcartype = entity.parkcartype,
            vehicleNo = entity.vehicleNo,
            parkFee = out?.parkfee?: 0,
            discountFee = out?.discountfee?: 0,
            dayDiscountFee = out?.dayDiscountfee?: 0,
            payFee = out?.payfee?: 0,
            payment = payment,
            nonPayment = out?.payfee?: 0 - payment
        )
    }
}