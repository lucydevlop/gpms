package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.dto.DiscountApplyDTO
import io.glnt.gpms.model.dto.EnterNotiDTO
import io.glnt.gpms.model.entity.ParkSiteInfo
import io.glnt.gpms.model.enums.*
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class ParkSiteInfoDTO (
    var siteId: String? = null,

    @get: NotNull
    var siteName: String? = null,

    var limitqty: Int? = 10,

    var saupno: String? = null,

    var tel: String? = null,

    var ceoname: String? = null,

    var postcode: String? = null,

    var city: CityType? = CityType.SEOUL,

    var address: String? = null,

    var firsttime: Int? = 30,

    var firstfee: Int? = 1000,

    var returntime: Int? = 20,

    var overtime: Int? = 30,

    var overfee: Int? = 1000,

    var addtime: Int? = 2,

    var dayfee: Int? = 20000,

    var parkingSpotStatusNotiCycle: Int? = 10,

    var facilitiesStatusNotiCycle: Int? = 10,

    var flagMessage: Int? = 0,

    var businame: String? = null,

    var parkId: String? = null,

    var space: Map<String, Any>? = null,

    @Enumerated(EnumType.STRING)
    var saleType: SaleType? = SaleType.FREE,

    @Enumerated(EnumType.STRING)
    var tmapSend: OnOff? = OnOff.OFF,

    @Enumerated(EnumType.STRING)
    var externalSvr: ExternalSvrType? = ExternalSvrType.NONE,

    var rcsParkId: Long? = null,

    var ip: String? = null,

    @Enumerated(EnumType.STRING)
    var vehicleDayOption: VehicleDayType? = VehicleDayType.OFF,

    @Enumerated(EnumType.STRING)
    var visitorExternal: VisitorExternalKeyType? = null,

    var visitorExternalKey: String? = null,

    @Enumerated(EnumType.STRING)
    var operatingDays: DiscountRangeType? = null,

    @Enumerated(EnumType.STRING)
    var visitorRegister: OnOff? = OnOff.ON,

    var enterNoti: EnterNotiDTO? = null,

    var discApply: DiscountApplyDTO? = null
): Serializable {
    constructor(parkSiteInfo: ParkSiteInfo) :
        this(
            parkSiteInfo.siteId, parkSiteInfo.siteName, parkSiteInfo.limitqty, parkSiteInfo.saupno,
            parkSiteInfo.tel, parkSiteInfo.ceoname, parkSiteInfo.postcode, parkSiteInfo.city, parkSiteInfo.address,
            parkSiteInfo.firsttime, parkSiteInfo.firstfee, parkSiteInfo.returntime, parkSiteInfo.overtime,
            parkSiteInfo.overfee, parkSiteInfo.addtime, parkSiteInfo.dayfee, parkSiteInfo.parkingSpotStatusNotiCycle,
            parkSiteInfo.facilitiesStatusNotiCycle, parkSiteInfo.flagMessage, parkSiteInfo.businame, parkSiteInfo.parkId,
            parkSiteInfo.space, parkSiteInfo.saleType, parkSiteInfo.tmapSend, parkSiteInfo.externalSvr, parkSiteInfo.rcsParkId,
            parkSiteInfo.ip, parkSiteInfo.vehicleDayOption, parkSiteInfo.visitorExternal, parkSiteInfo.visitorExternalKey,
            parkSiteInfo.operatingDays, parkSiteInfo.visitorRegister, parkSiteInfo.enterNoti, parkSiteInfo.discApply
        )
}