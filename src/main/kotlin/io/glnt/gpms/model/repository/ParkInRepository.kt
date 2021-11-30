package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ResultType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
interface ParkInRepository: JpaRepository<ParkIn, Long>, JpaSpecificationExecutor<ParkIn> {
    fun findByVehicleNoEndsWithAndOutSnAndGateIdAndDelYn(vehicleNo: String, outSn: Long, gateId: String, delYn: DelYn) : List<ParkIn>?
    fun findByUdpssid(udpssid: String): ParkIn?
//    fun findAll(specification: Specification<ParkIn>): List<ParkIn>?
    fun findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo: String, outSn: Long, delYn: DelYn, inDate: LocalDateTime ) : ParkIn?
    fun findBySn(sn: Long): ParkIn?
    fun findByUuid(uuid: String): ParkIn?
    fun countByGateIdAndOutSn(gateId: String, outSn: Long): Int
    fun countByGateIdInAndOutSn(gates: List<String>, outSn: Long): Int
    fun findTopByGateIdAndDelYnAndInDateGreaterThanEqualOrderByInDateDesc(gateId: String, delYn: DelYn, inDate: LocalDateTime ) : ParkIn?
    fun findTopByGateIdAndDelYnOrderByInDateDesc(gateId: String, delYn: DelYn) : ParkIn?
    fun findTopByOutSnAndDelYnOrderByInDateDesc(outSn: Long, delYn: DelYn) : ParkIn?
    fun findByUuidAndOutSnAndDelYn(uuid: String, outSn: Long, delYn: DelYn): List<ParkIn>?
    fun findByOutSnAndDelYn(outSn: Long, delYn: DelYn): List<ParkIn>?
    fun findByInDateBetweenAndDelYnAndOutSnGreaterThan(start: LocalDateTime, end: LocalDateTime, delYn: DelYn, outSn: Long): List<ParkIn>?
    fun findTopByVehicleNoAndOutSnGreaterThanAndDelYnOrderByInDateDesc(vehicleNo: String, outSn: Long, delYn: DelYn): ParkIn?
}

@Repository
interface ParkOutRepository: JpaRepository<ParkOut, Long>, JpaSpecificationExecutor<ParkOut> {
    fun findBySn(sn: Long): Optional<ParkOut>
    fun findByVehicleNoEndsWith(vehicleNo: String) : List<ParkOut>?
    fun findByInSnAndDelYn(inSn: Long, delYn: DelYn): Optional<ParkOut>
    fun findByRequestid(requestId: String): ParkOut?
    fun findByUuid(uuid: String): ParkOut?
    fun findTopByPaystationOrderByOutDateDesc(paystation: String): ParkOut?
    fun findTopByGateIdAndDelYnOrderByOutDateDesc(gateId: String, delYn: DelYn) : ParkOut?
    fun findTopByInSnAndDelYnOrderByOutDateDesc(inSn: Long, delYn: DelYn): ParkOut?
    fun findTopByInSnAndDelYnOrderByOutDateDescSnDesc(inSn: Long, delYn: DelYn): Optional<ParkOut>
    fun findByOutDateBetweenAndDelYn(start: LocalDateTime, end: LocalDateTime, delYn: DelYn): List<ParkOut>?
    fun findTopByVehicleNoAndGateIdAndInSnNotOrderByOutDateDesc(vehicleNo: String, gateId: String, inSn: Long): Optional<ParkOut>
}

@Repository
interface InoutPaymentRepository: JpaRepository<InoutPayment, Long>, JpaSpecificationExecutor<InoutPayment> {
    fun findByInSnAndResultAndDelYn(sn: Long, resultType: ResultType, delYn: DelYn): List<InoutPayment>?
    fun findByOutSnAndResultAndDelYn(sn: Long, resultType: ResultType, delYn: DelYn): List<InoutPayment>?
    fun findByInSnAndResultAndTransactionIdAndDelYn(sn: Long, resultType: ResultType, transactionId: String, delYn: DelYn): InoutPayment?
    fun findBySn(sn: Long): Optional<InoutPayment>

}