package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.TypeDefs
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_parkfeature",
    indexes = [Index(columnList = "feature_id", unique = true)])
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDefs(TypeDef(name = "jsonb", typeClass = JsonBinaryType::class),
    TypeDef(name = "string-array", typeClass = StringArrayType::class))
data class ParkFeature(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx", unique = true, nullable = false)
    var idx: Long?,

    @Column(name = "feature_id", nullable = false)
    var featureId: String,

    @Column(name = "flag", nullable = false)
    var flag: String = "0",

    @Column(name = "group_key", nullable = false)
    var groupKey: String,

    @Column(name = "category", nullable = false)
    var category: String,

    @Column(name = "transaction_id", nullable = false)
    var transactinoId: String,

    @Column(name = "connection_type", nullable = false)
    var connectionType: String,

    @Column(name = "ip", nullable = true)
    var ip: String?,

    @Column(name = "port", nullable = true)
    var port: String?,

//    @Type(type = "jsonb")
//    @Column(name = "path", nullable = true, columnDefinition = "jsonb")
//    var path: MutableSet<String>? = mutableSetOf(),
//    @Type(type = "string-array")
    @Column(name = "origin_img_path", nullable = true)
    var originImgPath: String? = null,

    @Column(name = "back_img_path", nullable = true)
    var backImgPath: String? = null,

    @Column(name = "assistant_img_path", nullable = true)
    var assistantImgPath: String? = null,

    @Column(name = "status", nullable = true)
    var status: String? = null

) : Auditable(), Serializable {

}
