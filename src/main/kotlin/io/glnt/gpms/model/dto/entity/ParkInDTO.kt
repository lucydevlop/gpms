package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class ParkInDTO(
    var sn: Long? = null,

    var gateId: String? = null,

    var parkcartype: String? = null,

    @get: NotNull
    var vehicleNo: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var inDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd")
    var date: LocalDate? = null,

    var image: String? = null,

    var resultcode: Int? = null,

    var outSn: Long? = null,

    var requestid: String? = null,

    var uuid: String? = null,

    var ticketSn: Long? = null,

    var memo: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    var seasonTicketDTO: SeasonTicketDTO? = null,

    var gate: GateDTO? = null

) : Serializable {
    constructor(parkIn: ParkIn) :
        this(
            parkIn.sn, parkIn.gateId, parkIn.parkcartype, parkIn.vehicleNo, parkIn.inDate, parkIn.date,
            parkIn.image, parkIn.resultcode, parkIn.outSn, parkIn.requestid, parkIn.uuid, parkIn.ticketSn,
            parkIn.memo, parkIn.delYn
        )
}
