package io.glnt.gpms.handler.user.model

import io.glnt.gpms.model.enums.UserRole
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqLogin (
    var id: String,
    var password: String
)

data class reqRegister(
    var id: String,
    var password: String,
    var userName: String,
    var userPhone: String?,
    @Enumerated(EnumType.STRING) var userRole: UserRole?
)

data class reqUserRegister(
    var corpId: String?,
    var password: String,
    var userName: String,
    var userPhone: String?,
    var corpName: String,
    var form: Int? = 1,
    var resident: Int? = 1,
    var dong: String?,
    var ho: String?,
    @Enumerated(EnumType.STRING) var userRole: UserRole?
)