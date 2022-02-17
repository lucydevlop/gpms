package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.JsonToMapConverter
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.SettingKey
import io.glnt.gpms.model.enums.YN
import org.hibernate.annotations.Type
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_setting")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Setting(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false, insertable = true, updatable = false)
    var sn: Long?,

    @Column(name = "enabled", nullable = false)
    @Type(type = "org.hibernate.type.NumericBooleanType")
    var enabled: Boolean? = false,

    @Enumerated(EnumType.STRING)
    @Column(name = "setting_key", nullable = false)
    var key: SettingKey? = null,

    @Type(type = "json")
    @Column(name = "value" , columnDefinition = "json")
    @Convert(attributeName = "value", converter = JsonToMapConverter::class)
    var value: Any? =null,

    @Column(name = "description")
    var description: String? = null
): Auditable(), Serializable {

}
