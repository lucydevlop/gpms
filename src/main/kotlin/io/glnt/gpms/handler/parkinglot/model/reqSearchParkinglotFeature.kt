package io.glnt.gpms.handler.parkinglot.model

import io.glnt.gpms.model.entity.Gate

data class reqSearchParkinglotFeature(
    var fromDate: String?,
    var toDate: String?,
    var relaySvrKey: String?,
    var featureId: String?,
    var facilitiesId: String?,
    var gateId: String?
)

data class reqCreateParkinglot(
    var siteId: String,
    var siteName: String,
    var limitqty: Int? = null,
    var saupno: String? = null,
    var tel: String? = null,
    var ceoname: String? = null,
    var postcode: String? = null,
    var address: String? = null,
    var firsttime: Int? = 30,
    var firstfee: Int? = 100,
    var returntime: Int? = 0,
    var overtime: Int? = 10,
    var overfee: Int? = 500,
    var addtime: Int? = 0,
    var dayfee: Int? = 20000,
    var parkingSpotStatusNotiCycle: Int? = null,
    var facilitiesStatusNotiCycle: Int? = null,
    var flagMessage: Int? = null,
    var businame: String? = null,
    var parkId: String? = null
)

data class reqUpdateGates(
    var gates: ArrayList<Gate>
)