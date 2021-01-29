package io.glnt.gpms.handler.corp.model

data class reqSearchCorp(
    var corpId: String? = null,
    var searchLabel: String? = null,
    var searchText: String? = null,
    var useStatus: String? = "ALL"
)
