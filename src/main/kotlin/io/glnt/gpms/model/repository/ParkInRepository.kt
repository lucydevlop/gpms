package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.ResultType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ParkInRepository: JpaRepository<ParkIn, Long>, JpaSpecificationExecutor<ParkIn> {
    fun findByVehicleNoEndsWithAndOutSnAndGateIdAndDelYn(vehicleNo: String, outSn: Long, gateId: String, delYn: YN) : List<ParkIn>?
    fun findByUdpssid(udpssid: String): ParkIn?
//    fun findAll(specification: Specification<ParkIn>): List<ParkIn>?
    fun findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo: String, outSn: Long, delYn: YN, inDate: LocalDateTime ) : ParkIn?
    fun findBySn(sn: Long): ParkIn?
    fun findByUuid(uuid: String): List<ParkIn>?
    fun countByGateIdInAndOutSn(gates: List<String>, outSn: Long): Int
    fun countByGateIdInAndOutSnGreaterThan(gates: List<String>, outSn: Long): Int
    fun findTopByGateIdAndDelYnAndInDateGreaterThanEqualOrderByInDateDesc(gateId: String, delYn: YN, inDate: LocalDateTime ) : ParkIn?
    fun findTopByGateIdAndDelYnOrderByInDateDesc(gateId: String, delYn: YN) : ParkIn?
    fun findTopByOutSnAndDelYnOrderByInDateDesc(outSn: Long, delYn: YN) : ParkIn?
    fun findByUuidAndOutSnAndDelYn(uuid: String, outSn: Long, delYn: YN): List<ParkIn>?
    fun findByOutSnAndDelYn(outSn: Long, delYn: YN): List<ParkIn>?
    fun findByInDateBetweenAndDelYnAndOutSnGreaterThan(start: LocalDateTime, end: LocalDateTime, delYn: YN, outSn: Long): List<ParkIn>?
    fun findTopByVehicleNoAndOutSnGreaterThanAndDelYnOrderByInDateDesc(vehicleNo: String, outSn: Long, delYn: YN): ParkIn?
}

@Repository
interface ParkOutRepository: JpaRepository<ParkOut, Long>, JpaSpecificationExecutor<ParkOut> {
    fun findBySn(sn: Long): Optional<ParkOut>
    fun findByVehicleNoEndsWith(vehicleNo: String) : List<ParkOut>?
    fun findByInSnAndDelYn(inSn: Long, delYn: YN): Optional<ParkOut>
    fun findByRequestid(requestId: String): ParkOut?
    fun findByUuid(uuid: String): ParkOut?
    fun findTopByPaystationOrderByOutDateDesc(paystation: String): ParkOut?
    fun findTopByGateIdAndDelYnOrderByOutDateDesc(gateId: String, delYn: YN) : ParkOut?
    fun findTopByInSnAndDelYnOrderByOutDateDesc(inSn: Long, delYn: YN): ParkOut?
    fun findTopByInSnAndDelYnOrderByOutDateDescSnDesc(inSn: Long, delYn: YN): Optional<ParkOut>
    fun findByOutDateBetweenAndDelYn(start: LocalDateTime, end: LocalDateTime, delYn: YN): List<ParkOut>?
    fun findTopByVehicleNoAndGateIdAndInSnNotOrderByOutDateDesc(vehicleNo: String, gateId: String, inSn: Long): Optional<ParkOut>
}

@Repository
interface InoutPaymentRepository: JpaRepository<InoutPayment, Long>, JpaSpecificationExecutor<InoutPayment> {
    fun findByInSnAndDelYn(sn: Long, delYN: YN): List<InoutPayment>?
    fun findByInSnAndResultAndDelYn(sn: Long, resultType: ResultType, delYN: YN): List<InoutPayment>?
    fun findByOutSnAndResultAndDelYn(sn: Long, resultType: ResultType, delYN: YN): List<InoutPayment>?
    fun findByInSnAndResultAndTransactionIdAndDelYn(sn: Long, resultType: ResultType, transactionId: String, delYN: YN): InoutPayment?
    fun findBySn(sn: Long): Optional<InoutPayment>
    fun findTopByInSnAndResultAndDelYnOrderBySnDesc(sn: Long, resultType: ResultType, delYN: YN): InoutPayment?

}