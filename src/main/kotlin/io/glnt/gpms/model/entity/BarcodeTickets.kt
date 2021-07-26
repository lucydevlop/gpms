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
    var sn: Long?,

    @Column(name = "barcode", nullable = false, unique = true)
    var barcode: String? = null,

    @Column(name = "in_sn", nullable = false)
    var inSn: Long,

    @Column(name = "vehicle_no")
    var vehicleNo: String? = null,

    @Column(name = "price")
    var price: Long? = null,

    @Column(name = "apply_date")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var applyDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {

}
