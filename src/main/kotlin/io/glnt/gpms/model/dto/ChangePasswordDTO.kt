package io.glnt.gpms.model.dto

import javax.validation.constraints.NotNull

data class ChangePasswordDTO(
    @get: NotNull
    var idx: Long? = null,

    @get: NotNull
    var password: String? = null,

    @get: NotNull
    var newPassword: String? = null,

    @get: NotNull
    var confirmPassword: String? = null
)
