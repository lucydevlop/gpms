package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_display_color")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DisplayColor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var  sn : Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "position", nullable = false)
    var position: DisplayPosition = DisplayPosition.IN,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: DisplayType = DisplayType.NORMAL1,

    @Column(name = "color_code")
    var colorCode: String,

    @Column(name = "color_desc")
    var colorDesc: String? = null
): Auditable(), Serializable {

}
