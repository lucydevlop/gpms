package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_tmapcommand")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class TmapCommand(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "type", nullable = true)
    var type: String? = null,

    @Column(name = "parkingSiteId", nullable = true)
    var parkingSiteId: String? = null,

    @Column(name = "responseId", nullable = true)
    var responseId: String? = null,

    @Column(name = "eventDateTime", nullable = true)
    var eventDateTime: String? = null,

    @Column(name = "contents")
    var contents: String? = null

) : Auditable(), Serializable {

}
