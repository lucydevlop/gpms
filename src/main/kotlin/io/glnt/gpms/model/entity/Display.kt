package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import java.io.Serializable
import javax.persistence.*
import kotlin.jvm.Transient

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

@Entity
@Table(schema = "glnt_parking", name="tb_display_message")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class DisplayMessage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var  sn : Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_class", nullable = true)
    var messageClass: DisplayMessageClass? = DisplayMessageClass.IN,

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    var messageType: DisplayMessageType = DisplayMessageType.NONMEMBER,

    @Column(name = "message_code")
    var messageCode: String = "ALL",

    @Column(name = "order_by")
    var order: Int? = 1,

    @Column(name = "line_number")
    var lineNumber: Int? = 1,


//    @Column(name = "line1_message", nullable = false)
//    var line1Message: String? = null,
//
//    @Enumerated(EnumType.STRING)
//    @Column(name = "line1_color_type", nullable = false)
//    var line1ColorType: DisplayType? = null,
//
//    @Column(name = "line2_message", nullable = false)
//    var line2Message: String? = null,
//
    @Enumerated(EnumType.STRING)
    @Column(name = "color_type", nullable = false)
    var colorType: DisplayType? = null,

    @Column(name = "message_desc")
    var messageDesc: String,

    @Transient
    var displayColor: DisplayColor? = null

//    ,
//    @Column(name = "color_desc")
//    var colorDesc: String? = null
): Auditable(), Serializable {
//    constructor(sn: Nothing?, messageClass: DisplayMessageClass, messageType: DisplayMessageType, colorType: DisplayType, order: Int, messageDesc: String) : this()


}
