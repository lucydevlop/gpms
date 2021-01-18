package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.GateTypeStatus
import org.hibernate.annotations.Where
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_gate")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Gate(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "gate_name", nullable = false)
    var gateName: String? = "GATE",

    @Column(name = "gate_id", nullable = false)
    var gateId: String = "GATE",

    @Enumerated(EnumType.STRING)
    @Column(name = "gate_type", nullable = false)
    var gateType: GateTypeStatus,

    @Column(name = "takeAction", nullable = false)
    var takeAction: String? = "GATE",

    @Column(name = "seasonTicketTakeAction", nullable = false)
    var seasonTicketTakeAction: String? = "GATE",

    @Column(name = "whiteListTakeAction", nullable = false)
    var whiteListTakeAction: String? = "OFF",

    @Column(name = "flag_use", nullable = false)
    var flagUse: Int? = 1,

    @Column(name = "udp_gateid", nullable = false)
    var udpGateid: String? = "GATE",

    @Column(name = "upload_ct", nullable = false)
    var udloadCt: Int? = 0,

    @Column(name = "relay_svr", nullable = false)
    var relaySvr: String? = "http://192.168.20.30:9999/v1"

//    ,
//    @JsonIgnore
//    @OneToMany(fetch = FetchType.LAZY, mappedBy = "gate_id")
//    var facilities: List<Facility> = ArrayList()

): Auditable(), Serializable {

//    @OneToMany//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
//    @JoinColumn(name = "gate_id")
////    @Where(clause = "delete_yn = 'N'")
//    var facilities: List<Facility> = emptyList()
}
