package io.glnt.gpms.handler.parkinglot.model

data class reqSearchParkinglotFeature(
    var fromDate: String?,
    var toDate: String?,
    var gateSvrKey: String?,
    var featureId: String?,
    var facilitiesId: String?
)
