package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.Setting
import io.glnt.gpms.model.enums.SettingKey
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class SettingDTO (
    var sn: Long? = null,

    var enabled: Boolean?,

    @get: NotNull
    var key: SettingKey? = null,

    var value: Any? = null,

    var description: String? = null,

    ) : Serializable {
    constructor(setting: Setting) :
            this(setting.sn, setting.enabled, setting.key, setting.value, setting.description)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SettingDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}