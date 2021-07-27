package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.DelYn
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_barcode_ticket")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BarcodeTickets(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long? = null,

    @Column(name = "barcode", nullable = false, unique = true)
    var barcode: String? = null,

    @Column(name = "in_sn", nullable = false)
    var inSn: Long? = null,

    @Column(name = "vehicle_no")
    var vehicleNo: String? = null,

    @Column(name = "price")
    var price: Int? = null,

    @Column(name = "apply_date")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var applyDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BarcodeTickets) return false

        return sn != null && other.sn != null && sn == other.sn
    }

    override fun hashCode() = 31

    override fun toString() = "BarcodeTickets{" +
        "sn=$sn" +
        ", barcode='$barcode'" +
        "}"

    companion object {
        private const val serialVersionUID = 1L
    }

}
