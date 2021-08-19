package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.model.enums.WeekType
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class FarePolicyDTO(
    var sn: Long?,

    @get: NotNull
    var fareName: String? = null,

    @Enumerated(EnumType.STRING)
    var vehicleType: VehicleType? = VehicleType.SMALL,

    @get: NotNull
    var startTime: String? = null,

    @get: NotNull
    var endTime: String? = null,

    @get: NotNull
    var basicFareSn: Long? = null,

    var addFareSn: Long? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = null,

    var week: MutableSet<String>? = mutableSetOf(WeekType.ALL.toString()),

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

    var basicFare: FareInfoDTO? = null,

    var addFare: FareInfoDTO? = null
): Serializable {
    constructor(farePolicy: FarePolicy) :
        this(
            farePolicy.sn, farePolicy.fareName, farePolicy.vehicleType, farePolicy.startTime, farePolicy.endTime,
            farePolicy.basicFareSn, farePolicy.addFareSn, farePolicy.effectDate, farePolicy.expireDate, farePolicy.week, farePolicy.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FarePolicyDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}
