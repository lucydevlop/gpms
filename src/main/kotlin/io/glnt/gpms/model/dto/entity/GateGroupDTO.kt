package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.GateGroup
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class GateGroupDTO(
    var id: Long?,

    var gateGroupId: String? = null,

    var gateGroupName: String? = null,

    var location: String? = null,

    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null
): Serializable {
    constructor(gateGroup: GateGroup) :
        this(
            gateGroup.id, gateGroup.gateGroupId, gateGroup.gateGroupName, gateGroup.location, gateGroup.memo, gateGroup.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is GateGroupDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}