package io.glnt.gpms.handler.auth.model

import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.entity.SiteUser

data class resLogin(
    var token: String,
    var userInfo: SiteUser,
    var corpInfo: Corp? = null
)
