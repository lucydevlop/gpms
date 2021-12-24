package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.JsonToMapConverter
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.GateTypeStatus
import io.glnt.gpms.model.enums.OpenActionType
import org.hibernate.annotations.Type
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
    var uploadCt: Int? = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "open_action", nullable = false)
    var openAction: OpenActionType? = OpenActionType.NONE,

    @Column(name = "relay_svr_key", nullable = false)
    var relaySvrKey: String? = "RELAYSVR1",

    @Column(name = "relay_svr", nullable = false)
    var relaySvr: String? = "http://192.168.20.30:9999/v1",

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: YN? = YN.N,

    @Column(name = "reset_svr", nullable = false)
    var resetSvr: String? = "http://192.168.20.211/io.cgi?relay=",

    @Column(name = "gate_group_id")
    var gateGroupId: String? = null,

    @Type(type = "json")
    @Column(name = "open_type", columnDefinition = "json")
    @Convert(attributeName = "open_type", converter = JsonToMapConverter::class)
    var openType: ArrayList<Map<String, Any>>? = null,

    ): Auditable(), Serializable {

    @OneToOne
    @JoinColumn(name = "gate_group_id", referencedColumnName = "gate_group_id", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var gateGroup: GateGroup? = null
}
