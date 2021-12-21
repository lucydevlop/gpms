package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.InoutPaymentDTO
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.mapper.InoutPaymentMapper
import io.glnt.gpms.model.repository.InoutPaymentRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
open class InoutPaymentService (
    private var inoutPaymentRepository: InoutPaymentRepository,
    private var inoutPaymentMapper: InoutPaymentMapper
){
    companion object : KLogging()

    @Transactional(readOnly = true)
    open fun findAll(): List<InoutPaymentDTO> {
        return inoutPaymentRepository.findAll().map(inoutPaymentMapper::toDTO)
    }

    fun findOne(sn: Long): Optional<InoutPaymentDTO> {
        logger.debug { "Request to get InoutPayment $sn" }
        return inoutPaymentRepository.findBySn(sn).map(inoutPaymentMapper::toDTO)
    }

    @Transactional(readOnly = true)
    open fun findByInSnAndResult(sn: Long, result: ResultType): List<InoutPaymentDTO>? {
        logger.debug { "Request to get InoutPayment $sn" }
        return inoutPaymentRepository.findByInSnAndResultAndDelYn(sn, result, DelYn.N)?.map(inoutPaymentMapper::toDTO)
    }

    @Transactional(readOnly = true)
    open fun findByInSn(sn: Long): List<InoutPaymentDTO>? {
        logger.debug { "Request to get InoutPayment $sn" }
        return inoutPaymentRepository.findByInSnAndDelYn(sn, DelYn.N)?.map(inoutPaymentMapper::toDTO)
    }

    fun save(inoutPaymentDTO: InoutPaymentDTO) : InoutPaymentDTO {
        inoutPaymentMapper.toEntity(inoutPaymentDTO)?.let { inoutPayment ->
            inoutPaymentDTO.sn?.let {
                inoutPaymentRepository.saveAndFlush(inoutPayment)
                return inoutPaymentMapper.toDTO(inoutPayment)
            }?: kotlin.run {
                // 중복 데이터 check 후 save
                if (inoutPayment.failureMessage == null && inoutPayment.transactionId != null) {
                    inoutPaymentRepository.findByInSnAndResultAndTransactionIdAndDelYn(inoutPayment.inSn ?: -1, ResultType.SUCCESS, inoutPayment.transactionId!!, DelYn.N)?.let { it ->
                        return inoutPaymentMapper.toDTO(it)
                    }
                } else {
                    inoutPaymentRepository.saveAndFlush(inoutPayment)
                    return inoutPaymentMapper.toDTO(inoutPayment)
                }
            }
        }
        return inoutPaymentDTO
    }
}