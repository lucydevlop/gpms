package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.FacilityCategoryType
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.LprTypeStatus
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_facilities")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Facility (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    var category: FacilityCategoryType? = FacilityCategoryType.LPR,

    @Column(name = "modelid", nullable = false)
    var modelId: String,

    @Column(name = "f_name", nullable = false)
    var fName: String,

    @Column(name = "dtFacilitiesId", nullable = false)
    var dtFacilitiesId: String,

    @Column(name = "FacilitiesId", nullable = true)
    var facilitiesId: String? = null,

    @Column(name = "flag_use", nullable = true)
    var flagUse: Int? = 1,

    @Column(name = "gate_id", nullable = false)
    var gateId: String,

    @Column(name = "udp_gateid", nullable = true)
    var udpGateId: String? = null,

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
    @Column(name = "lpr_type")
    var lprType: LprTypeStatus? = null,

    @Column(name = "image_path")
    var imagePath: String? = null,

    @Column(name = "health")
    var health: String? = null,

    @Column(name = "health_date")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var healthDate: LocalDateTime? = null,

    @Column(name = "status")
    var status: String? = null,

    @Column(name = "status_date")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var statusDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: YN? = YN.N,

    @Enumerated(EnumType.STRING)
    @Column(name = "gate_type", nullable = false)
    var gateType: GateTypeStatus? = GateTypeStatus.IN
//    ,
//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name = "gate_id", nullable = false)
//    var gateInfo: Gate

) : Auditable(), Serializable {

//    @OneToOne
//    @JoinColumn(name = "gate_id", referencedColumnName="gate_id", insertable = false, updatable = false)
//    var gate: Gate? = null
}