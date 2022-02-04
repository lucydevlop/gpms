package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.OpenActionType
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class GateDTO(
    var sn: Long?,

    @get: NotNull
    var gateName: String? = null,

    @get: NotNull
    var gateId: String? = null,

    @Enumerated(EnumType.STRING)
    var gateType: GateTypeStatus? = null,

    var takeAction: String? = null,

    var udpGateId: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var openAction: OpenActionType? = null,

    var relaySvrKey: String? = null,

    var relaySvr: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    var resetSvr: String? = null,

    var gateGroupId: String? = null,

    var openType: ArrayList<Map<String, Any>>? = null,

    var gateGroup: GateGroupDTO? = null
) : Serializable {
    constructor(gate: Gate) :
        this(
            gate.sn, gate.gateName, gate.gateId, gate.gateType, gate.takeAction, gate.udpGateId,
            gate.openAction, gate.relaySvrKey, gate.relaySvr, gate.delYn, gate.resetSvr, gate.gateGroupId, gate.openType
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GateDTO) return false
        return gateId != null && gateId == other.gateId
    }

    override fun hashCode() = 31
}
