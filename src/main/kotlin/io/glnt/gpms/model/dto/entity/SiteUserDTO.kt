package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.enums.checkUseStatus
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class SiteUserDTO(
    var idx: Long? = null,

    @get: NotNull
    var id: String? = null,

    @get: NotNull
    var password: String? = null,

    @get: NotNull
    var userName: String? = null,

    var userPhone: String? = null,

    var userEmail: String? = null,

    @Enumerated(value = EnumType.STRING)
    var checkUse: checkUseStatus? = null,

    var wrongCount: Int? = null,

    var passwordDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    var role: UserRole? = null,

    var loginDate: LocalDateTime? = null,

    var corpSn: Long? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null,

) : Serializable {
    constructor(siteUser: SiteUser) :
            this(
                siteUser.idx, siteUser.id, siteUser.password, siteUser.userName, siteUser.userPhone, siteUser.userEmail,
                siteUser.checkUse, siteUser.wrongCount, siteUser.passwordDate, siteUser.role, siteUser.loginDate,
                siteUser.corpSn, siteUser.delYn
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SiteUserDTO) return false
        return id != null && id == other.id
    }

    override fun hashCode() = 31
}
