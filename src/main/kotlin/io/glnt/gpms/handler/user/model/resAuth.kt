package io.glnt.gpms.handler.user.model

import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.entity.SiteUser

data class resLogin(
    var token: String? = null,
    var userInfo: SiteUser,
    var corpInfo: Corp? = null
)
