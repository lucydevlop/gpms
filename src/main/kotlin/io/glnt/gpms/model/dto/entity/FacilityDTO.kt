package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.Facility
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.LprTypeStatus
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class FacilityDTO(
    var sn: Long?,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var category: FacilityCategoryType? = null,

    @get: NotNull
    var modelId: String? = null,

    @get: NotNull
    var fName: String? = null,

    @get: NotNull
    var dtFacilitiesId: String? = null,

    var facilitiesId: String? = null,

    @get: NotNull
    var gateId: String? = null,

    var udpGateId: String? = null,

    var ip: String? = null,

    var port: String? = null,

    var sortCount: Int? = 1,

    var resetPort: Int? = null,

    var flagConnect: Int? = null,

    @Enumerated(EnumType.STRING)
    var lprType: LprTypeStatus? = null,

    var imagePath: String? = null,

    var health: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var healthDate: LocalDateTime? = null,

    var status: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var statusDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    @Enumerated(EnumType.STRING)
    var gateType: GateTypeStatus? = null,

    var relaySvrKey: String? = null,

    var checkTime: Int? = null,

    var counterResetTime: Int? = null
): Serializable {
    constructor(facility: Facility) :
        this(
            facility.sn, facility.category, facility.modelId, facility.fName, facility.dtFacilitiesId,
            facility.facilitiesId, facility.gateId, facility.udpGateId, facility.ip, facility.port,
            facility.sortCount, facility.resetPort, facility.flagConnect, facility.lprType, facility.imagePath,
            facility.health, facility.healthDate, facility.status, facility.statusDate, facility.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FacilityDTO) return false
        return dtFacilitiesId != null && dtFacilitiesId == other.dtFacilitiesId
    }

    override fun hashCode() = 31
}
