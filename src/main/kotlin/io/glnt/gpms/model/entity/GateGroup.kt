package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_gate_group")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class GateGroup (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    var id: Long?,

    @Column(name = "gate_group_id", unique = true, nullable = false)
    var gateGroupId: String = "GATEGROUP1",

    @Column(name = "gate_group_name", unique = true, nullable = false)
    var gateGroupName: String? = "GATEGROUP1",

    @Column(name = "location", nullable = true)
    var location: String? = null,

    @Column(name = "memo", nullable = true)
    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = true)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {
}