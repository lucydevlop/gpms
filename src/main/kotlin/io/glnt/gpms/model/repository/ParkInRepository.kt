package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.InoutPayment
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.ResultType
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ParkInRepository: JpaRepository<ParkIn, Long> {
    fun findByVehicleNoEndsWithAndOutSnAndGateId(vehicleNo: String, outSn: Long, gateId: String) : List<ParkIn>?
    fun findByUdpssid(udpssid: String): ParkIn?
    fun findAll(specification: Specification<ParkIn>): List<ParkIn>?
    fun findTopByVehicleNoAndOutSnAndDelYnAndInDateLessThanEqualOrderByInDateDesc(vehicleNo: String, outSn: Long, delYn: DelYn, inDate: LocalDateTime ) : ParkIn?
    fun findBySn(sn: Long): ParkIn?
    fun findByUuid(uuid: String): ParkIn?
    fun countByGateIdAndOutSn(gateId: String, outSn: Long): Int
    fun findTopByGateIdAndDelYnAndInDateGreaterThanEqualOrderByInDateDesc(gateId: String, delYn: DelYn, inDate: LocalDateTime ) : ParkIn?
    fun findTopByGateIdAndDelYnOrderByInDateDesc(gateId: String, delYn: DelYn) : ParkIn?
    fun findTopByOutSnAndDelYnOrderByInDateDesc(outSn: Long, delYn: DelYn) : ParkIn?
    fun findByUuidAndOutSnAndDelYn(uuid: String, outSn: Long, delYn: DelYn): List<ParkIn>?
    fun findByOutSnAndDelYn(outSn: Long, delYn: DelYn): List<ParkIn>?
}

@Repository
interface ParkOutRepository: JpaRepository<ParkOut, Long> {
    fun findBySn(sn: Long): ParkOut?
    fun findByVehicleNoEndsWith(vehicleNo: String) : List<ParkOut>?
    fun findAll(specification: Specification<ParkOut>): List<ParkOut>?
    fun findByRequestid(requestId: String): ParkOut?
    fun findByUuid(uuid: String): ParkOut?
    fun findTopByPaystationAndApproveDatetimeIsNotNullOrderByOutDateDesc(paystation: String): ParkOut?
    fun findTopByPaystationOrderByOutDateDesc(paystation: String): ParkOut?
    fun findTopByGateIdAndDelYnAndOutDateGreaterThanEqualOrderByOutDateDesc(gateId: String, delYn: DelYn, inDate: LocalDateTime ) : ParkOut?
    fun findTopByGateIdAndDelYnOrderByOutDateDesc(gateId: String, delYn: DelYn) : ParkOut?
}

@Repository
interface InoutPaymentRepository: JpaRepository<InoutPayment, Long> {
    fun findByInSnAndResultAndDelYn(sn: Long, resultType: ResultType, delYn: DelYn): List<InoutPayment>?

}