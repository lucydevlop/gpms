package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.enums.DelYn
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_parkin")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ParkIn(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "sitenum", nullable = true)
    var sitenum: Int? = 1,

    @Column(name = "groupnum", nullable = true)
    var groupnum: Int? = 1,

    @Column(name = "gate_id")
    var gateId: String? = null,

    @Column(name = "parkcartype", nullable = true)
    var parkcartype: String? = "일반차량",

    @Column(name = "user_sn", nullable = true)
    var userSn: Int? = 0,

    @Column(name = "vehicleNo", nullable = true)
    var vehicleNo: String? = null,

    @Column(name = "devicenum", nullable = true)
    var devicenum: Int? = null,

    @Column(name = "in_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var inDate: LocalDateTime? = null,

    @Column(name = "date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd")
    var date: LocalDate? = LocalDate.now(),

    @Column(name = "hour", nullable = true)
    var hour: String? = null,

    @Column(name = "min", nullable = true)
    var min: String? = null,

    @Column(name = "image", nullable = true)
    var image: String? = null,

    @Column(name = "flag", nullable = true)
    var flag: Int? = 0,

    @Column(name = "validate", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var validate: LocalDateTime? = null,

    @Column(name = "out_sn", nullable = true)
    var outSn: Long? = 0,

    @Column(name = "back_sn", nullable = true)
    var backSn: Int? = 0,

    @Column(name = "add_sn", nullable = true)
    var addSn: Int? = 0,

    @Column(name = "resultcode", nullable = true)
    var resultcode: Int? = null,

    @Column(name = "udpssid", nullable = true)
    var udpssid: String? = null,

    @Column(name = "requestid", nullable = true)
    var requestid: String? = null,

    @Column(name = "fileuploadid", nullable = true)
    var fileuploadid: String? = null,

    @Column(name = "invehicleRequestId", nullable = true)
    var invehicleRequestId: String? = null,

    @Column(name = "uuid", nullable = true)
    var uuid: String? = null,

    @Column(name = "ticket_sn", nullable = true)
    var ticketSn: Long? = null,

    @Column(name = "memo", nullable = true)
    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N
): Auditable(), Serializable {
    @OneToOne
    @JoinColumn(name = "ticket_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    var ticket: ProductTicket? = null
}
