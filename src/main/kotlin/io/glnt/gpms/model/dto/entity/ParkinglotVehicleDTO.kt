package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.ParkinglotVehicle
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.GateTypeStatus
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class ParkinglotVehicleDTO(
    var sn: Long? = null,

    @get: NotNull
    var date: LocalDateTime? = null,

    var vehicleNo: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var type: GateTypeStatus? = null,

    var uuid: String? = null,

    var image: String? = null,

    var memo: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null
): Serializable {

    constructor(parkinglotVehicle: ParkinglotVehicle) :
        this(
            parkinglotVehicle.sn, parkinglotVehicle.date, parkinglotVehicle.vehicleNo, parkinglotVehicle.type,
            parkinglotVehicle.uuid, parkinglotVehicle.image, parkinglotVehicle.memo, parkinglotVehicle.delYn
        )


}
