package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.LprTypeStatus
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_facilities",
    indexes = [Index(columnList = "FacilitiesId", unique = true)])
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Facility (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "category", nullable = false)
    var category: String,

    @Column(name = "modelid", nullable = false)
    var modelid: String,

    @Column(name = "f_name", nullable = false)
    var fname: String,

    @Column(name = "dtFacilitiesId", nullable = false)
    var dtFacilitiesId: String,

    @Column(name = "FacilitiesId", nullable = true)
    var facilitiesId: String? = null,

    @Column(name = "flag_use", nullable = true)
    var flagUse: Int? = 1,

    @Column(name = "gate_id", nullable = false)
    var gateId: String,

    @Column(name = "udp_gateid", nullable = true)
    var udpGateid: String? = null,

    @Column(name = "ip", nullable = true)
    var ip: String? = "0.0.0.0",

    @Column(name = "port", nullable = true)
    var port: String? = "0",

    @Column(name = "sort_count")
    var sortCount: Int? = 1,

    @Column(name = "reset_port")
    var resetPort: Int? = 0,

    @Column(name = "flag_connect")
    var flagConnect: Int? = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "gate_type")
    var gateType: GateTypeStatus,

    @Enumerated(EnumType.STRING)
    @Column(name = "lpr_type")
    var lprType: LprTypeStatus? = null,

    @Column(name = "image_path")
    var imagePath: String? = null,

    @Column(name = "gate_svr_key")
    var gateSvrKey: String? = null
//    ,
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "gate_id", nullable = false)
//    var gateInfo: Gate

) : Auditable(), Serializable {

//    @OneToOne
//    @JoinColumn(name = "gate_id", referencedColumnName="gate_id", insertable = false, updatable = false)
//    var gate: Gate? = null
}