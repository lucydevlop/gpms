package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.OnOff
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class EnterNotiDTO(

    @Enumerated(EnumType.STRING)
    var use: OnOff? = null,

    var url: String? = null
)
