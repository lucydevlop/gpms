package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.InoutPaymentDTO
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.ResultType
import io.glnt.gpms.model.mapper.InoutPaymentMapper
import io.glnt.gpms.model.repository.InoutPaymentRepository
import mu.KLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDate
import java.util.*

@Service
open class InoutPaymentService (
    private var inoutPaymentRepository: InoutPaymentRepository,
    private var inoutPaymentMapper: InoutPaymentMapper
){
    @Value("\${receipt.filepath}")
    lateinit var receiptPath: String

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
        return inoutPaymentRepository.findByInSnAndResultAndDelYn(sn, result, YN.N)?.map(inoutPaymentMapper::toDTO)
    }

    @Transactional(readOnly = true)
    open fun findByInSn(sn: Long): List<InoutPaymentDTO>? {
        logger.debug { "Request to get InoutPayment $sn" }
        return inoutPaymentRepository.findByInSnAndDelYn(sn, YN.N)?.map(inoutPaymentMapper::toDTO)
    }

    fun save(inoutPaymentDTO: InoutPaymentDTO) : InoutPaymentDTO {
        inoutPaymentMapper.toEntity(inoutPaymentDTO)?.let { inoutPayment ->
            inoutPaymentDTO.sn?.let {
                inoutPaymentRepository.saveAndFlush(inoutPayment)
                return inoutPaymentMapper.toDTO(inoutPayment)
            }?: kotlin.run {
                // 중복 데이터 check 후 save
                if (inoutPayment.failureMessage == null && inoutPayment.transactionId != null) {
                    inoutPaymentRepository.findByInSnAndResultAndTransactionIdAndDelYn(inoutPayment.inSn ?: -1, ResultType.SUCCESS, inoutPayment.transactionId!!, YN.N)?.let { it ->
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

    fun uploadReceipt(sn: Long, file: MultipartFile) {
        try {
            val fileFullPath: String = "$receiptPath/"+ LocalDate.now()
            File(fileFullPath).apply {
                if (!exists()) {
                    mkdirs()
                }
            }

            val fileName : String = "$fileFullPath/" + (file.originalFilename ?: ("$sn.jpg"))
            val path = Paths.get(fileName)
            Files.copy(file.inputStream, path, StandardCopyOption.REPLACE_EXISTING)

            findOne(sn).ifPresent { inoutPayment ->
                inoutPayment.receipt = fileName
                save(inoutPayment)
            }
        }catch (e:Exception){
            throw Exception(e.message)
        }
    }
}