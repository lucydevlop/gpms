package io.glnt.gpms.handler.corp.model

import io.glnt.gpms.model.enums.DelYn
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqSearchCorp(
    var corpId: String? = null,
    var searchLabel: String? = null,
    var searchText: String? = null,
    @Enumerated(EnumType.STRING) var useStatus: DelYn? = null
)
