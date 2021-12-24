package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.Setting
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class SettingDTO (
    var sn: Long? = null,

    @get: NotNull
    var code: String? = null,

    var value: String? = null,

    var description: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null,

    ) : Serializable {
    constructor(setting: Setting) :
            this(setting.sn, setting.code, setting.value, setting.description, setting.delYn)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SettingDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}