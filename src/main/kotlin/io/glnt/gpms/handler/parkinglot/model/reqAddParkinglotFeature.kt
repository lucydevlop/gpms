package io.glnt.gpms.handler.parkinglot.model

data class reqAddParkinglotFeature(
    var featureId: String,
    var groupKey: String,
    var category: String,
    var transactionId: String,
    var connetionType: String,
    var ip: String? = null,
    var port: String? = null,
//    var path: MutableSet<String>? = null,
    var path : String? = null,
    var flag: String = "0"
)
