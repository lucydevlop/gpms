package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.common.collect.Multiset
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.ExternalSvrType
import io.glnt.gpms.model.enums.OnOff
import io.glnt.gpms.model.enums.SaleType
import io.glnt.gpms.model.enums.VehicleDayType
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
    var siteid: String,

    @Column(name = "sitename", nullable = false)
    var sitename: String,

    @Column(name = "limitqty", nullable = true)
    var limitqty: Int? = 10,

    @Column(name = "saupno", nullable = true)
    var saupno: String? = null,

    @Column(name = "tel", nullable = true)
    var tel: String? = null,

    @Column(name = "ceoname", nullable = true)
    var ceoname: String? = null,

    @Column(name = "postcode", nullable = true)
    var postcode: String? = null,

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

    @Lob
//    @Type(type = "jsonb")
    @Column(name = "space", nullable = true)
    var space: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_type", nullable = true)
    var saleType: SaleType? = SaleType.FREE,

    @Enumerated(EnumType.STRING)
    @Column(name = "tmap_send", nullable = true)
    var tmapSend: OnOff? = OnOff.OFF,

    @Enumerated(EnumType.STRING)
    @Column(name = "external_svr", nullable = true)
    var externalSvr: ExternalSvrType? = ExternalSvrType.NONE,

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_day_option")
    var vehicleDayOption: VehicleDayType? = VehicleDayType.OFF
) : Auditable(), Serializable {

    data class spaceAttributes(
        var spaces: MutableSet<spaceAttribute>? = null
    )
    data class spaceAttribute(
        @JsonProperty("gate")
        var gate: List<String>,
        @JsonProperty("space")
        var space: Int
    )
}

//@JsonInclude(JsonInclude.Include.NON_NULL)

