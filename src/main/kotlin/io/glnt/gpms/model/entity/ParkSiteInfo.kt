package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import io.glnt.gpms.common.utils.JsonToMapConverter
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.dto.DiscountApplyDTO
import io.glnt.gpms.model.dto.EnterNotiDTO
import io.glnt.gpms.model.enums.*
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_parksiteinfo")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDef(name = "jsonb", typeClass = JsonBinaryType::class)
data class ParkSiteInfo(
    @Id
    @Column(name = "siteid", unique = true, nullable = false)
    var siteId: String,

    @Column(name = "sitename", nullable = false)
    var siteName: String,

    @Column(name = "limitqty", nullable = true)
    var limitqty: Int? = 10,

    @Column(name = "saupno", nullable = true)
    var saupNo: String? = null,

    @Column(name = "tel", nullable = true)
    var tel: String? = null,

    @Column(name = "ceoname", nullable = true)
    var ceoName: String? = null,

    @Column(name = "postcode", nullable = true)
    var postCode: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "city")
    var city: CityType? = CityType.SEOUL,

    @Column(name = "address", nullable = true)
    var address: String? = null,

    @Column(name = "firsttime", nullable = true)
    var firsttime: Int? = 30,

    @Column(name = "firstfee", nullable = true)
    var firstfee: Int? = 1000,

    @Column(name = "returntime", nullable = true)
    var returntime: Int? = 20,

    @Column(name = "overtime", nullable = true)
    var overtime: Int? = 30,

    @Column(name = "overfee", nullable = true)
    var overfee: Int? = 1000,

    @Column(name = "addtime", nullable = true)
    var addtime: Int? = 2,

    @Column(name = "dayfee", nullable = true)
    var dayfee: Int? = 20000,

    @Column(name = "parkingSpotStatusNotiCycle", nullable = true)
    var parkingSpotStatusNotiCycle: Int? = 10,

    @Column(name = "facilitiesStatusNotiCycle", nullable = true)
    var facilitiesStatusNotiCycle: Int? = 10,

    @Column(name = "flag_message", nullable = true)
    var flagMessage: Int? = 0,

    @Column(name = "businame", nullable = true)
    var businame: String? = null,

    @Column(name = "park_id", nullable = true)
    var parkId: String? = null,

    @Type(type = "json")
    @Column(name = "space", columnDefinition = "json")
    @Convert(attributeName = "space", converter = JsonToMapConverter::class)
//    val menuJson:
//    @Column(name = "space", nullable = true)
    var space: Map<String, Any>? = null, //emptyMap(),

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = true)
    var saleType: SaleType? = SaleType.FREE,

    @Enumerated(EnumType.STRING)
    @Column(name = "tmap_send", nullable = true)
    var tmapSend: OnOff? = OnOff.OFF,

    @Enumerated(EnumType.STRING)
    @Column(name = "external_svr", nullable = true)
    var externalSvr: ExternalSvrType? = ExternalSvrType.NONE,

    @Column(name = "rcs_park_id", nullable = true)
    var rcsParkId: Long? = null,

    @Column(name = "ip")
    var ip: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_day_option")
    var vehicleDayOption: VehicleDayType? = VehicleDayType.OFF,

    @Enumerated(EnumType.STRING)
    @Column(name = "visitor_external")
    var visitorExternal: VisitorExternalKeyType? = null,

    @Column(name = "visitor_external_key")
    var visitorExternalKey: String? = null,

    @Enumerated(EnumType.STRING)
    var operatingDays: DiscountRangeType? = DiscountRangeType.ALL,

    @Enumerated(EnumType.STRING)
    var visitorRegister: OnOff? = OnOff.ON,

    @Type(type = "json")
    @Column(name = "enter_noti", columnDefinition = "json")
    @Convert(attributeName = "enter_noti", converter = JsonToMapConverter::class)
    var enterNoti: EnterNotiDTO? = null, //emptyMap(),

    @Type(type = "json")
    @Column(name = "disc_apply", columnDefinition = "json")
    @Convert(attributeName = "disc_apply", converter = JsonToMapConverter::class)
    var discCriteria: DiscountApplyDTO? = null //emptyMap(),
) : Auditable(), Serializable {

}

//@JsonInclude(JsonInclude.Include.NON_NULL)

