package io.glnt.gpms.handler.auth.model

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