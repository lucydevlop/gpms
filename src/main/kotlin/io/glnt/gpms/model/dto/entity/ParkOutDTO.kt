package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.dto.entity.GateDTO
import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class ParkOutDTO(
    var sn: Long? = null,

    var gateId: String? = null,

    var parkcartype: String? = null,

    @get: NotNull
    var vehicleNo: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var outDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd")
    var date: LocalDate? = null,

    var image: String? = null,

    var resultcode: Int? = null,

    var inSn: Long? = null,

    var requestid: String? = null,

    var uuid: String? = null,

    var parktime: Int? = null,

    var parkfee: Int? = null,

    var payfee: Int? = null,

    var discountfee: Int? = null,

    var dayDiscountfee: Int? = null,

    var fileuploadid: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var gate: GateDTO? = null,

    var parkInDTO: ParkInDTO? = null,

    // non entity
    var originParkFee: Int? = null,

    var originPayFee: Int? = null,

    var originDiscountFee: Int? = null,

    var originDayDiscountFee: Int? = null,

    var originParkTime: Int? = null
): Serializable {
    constructor(parkOut: ParkOut) :
        this(
            parkOut.sn, parkOut.gateId, parkOut.parkcartype, parkOut.vehicleNo, parkOut.outDate,
            parkOut.date, parkOut.image, parkOut.resultcode, parkOut.inSn, parkOut.requestid,
            parkOut.uuid, parkOut.parktime, parkOut.parkfee, parkOut.payfee, parkOut.discountfee,
            parkOut.dayDiscountfee, parkOut.fileuploadid, parkOut.delYn
        )
}
