package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.GateTypeStatus
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_parkinglot_vehicle")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ParkinglotVehicle (
    @EmbeddedId
    var id: ParkinglotVehicleId? = null,

    var vehicleNo: String? = null,

    var type: GateTypeStatus? = null,

    var uuid: String? = null,

    var image: String? = null,

    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParkinglotVehicle) return false

        return id != null && other.id != null && id == other.id
    }

    override fun hashCode() = 31

    override fun toString() = "ParkinglotVehicle{" +
        "id=$id" +
        ", vehicleNo='$vehicleNo'" +
        ", type='$type'" +
        ", uuid='$uuid'" +
        ", image='$image'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }
}

@Embeddable
data class ParkinglotVehicleId (
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var date: LocalDateTime? = null
): Serializable {

}