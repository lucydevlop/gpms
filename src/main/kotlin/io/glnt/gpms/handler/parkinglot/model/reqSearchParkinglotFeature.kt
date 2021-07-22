package io.glnt.gpms.handler.parkinglot.model

import io.glnt.gpms.model.entity.Gate
import io.glnt.gpms.model.enums.*

data class reqSearchParkinglotFeature(
    var fromDate: String? = null,
    var toDate: String? = null,
    var relaySvrKey: String? = null,
    var featureId: String? = null,
    var facilitiesId: String? = null,
    var gateId: String? = null
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
    var parkId: String? = null,
    var vehicleDayOption: VehicleDayType? = VehicleDayType.OFF,
    var tmapSend: OnOff? = OnOff.OFF,
    var saleType: SaleType? = SaleType.FREE,
    var externalSvr: ExternalSvrType? = ExternalSvrType.NONE,
    var ip: String? = null,
    var city: CityType? = null,
    var space: Map<String, Any>? = null,
    var visitorExternal: VisitorExternalKeyType? = null,
    var visitorExternalKey: String? = null
)

data class reqUpdateGates(
    var gates: ArrayList<Gate>
)