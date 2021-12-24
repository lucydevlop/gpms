package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_parkout")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ParkOut(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "sitenum", nullable = true)
    var sitenum: Int? = 1,

    @Column(name = "groupnum", nullable = true)
    var groupnum: Int? = 1,

    @Column(name = "parkcartype", nullable = true)
    var parkcartype: String? = "일반차량",

    @Column(name = "user_sn", nullable = true)
    var userSn: Int? = 0,

    @Column(name = "vehicleNo", nullable = true)
    var vehicleNo: String? = null,

    @Column(name = "devicenum", nullable = true)
    var devicenum: Int? = null,

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

    @Column(name = "parktime", nullable = true)
    var parktime: Int? = 0,

    @Column(name = "parkfee", nullable = true)
    var parkfee: Int? = 0,

    @Column(name = "payfee", nullable = true)
    var payfee: Int? = 0,

    @Column(name = "discountfee", nullable = true)
    var discountfee: Int? = 0,

    @Column(name = "dayDiscountfee", nullable = true)
    var dayDiscountfee: Int? = 0,

    @Column(name = "discountType", nullable = true)
    var discountType: Int? = 0,

    @Column(name = "validate", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var validate: LocalDateTime? = null,

    @Column(name = "resultcode", nullable = true)
    var resultcode: Int? = 0,

    @Column(name = "requestid", nullable = true)
    var requestid: String? = null,

    @Column(name = "fileuploadid", nullable = true)
    var fileuploadid: String? = null,

    @Column(name = "cardtransactionid", nullable = true)
    var cardtransactionid: String? = null,

    @Column(name = "chargingId", nullable = true)
    var chargingId: String? = null,

    @Column(name = "outVehicle", nullable = true)
    var outVehicle: Int? = 0,

    @Column(name = "approveDatetime", nullable = true)
    var approveDatetime: String? = null,

    @Column(name = "paystation", nullable = true)
    var paystation: String? = null,

    @Column(name = "cardNumber", nullable = true)
    var cardNumber: String? = null,

    @Column(name = "uuid", nullable = true)
    var uuid: String? = null,

    @Column(name = "gate_id")
    var gateId: String? = null,

    @Column(name = "out_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var outDate: LocalDateTime? = null,

    @Column(name = "in_sn")
    var inSn: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: YN? = YN.N
): Auditable(), Serializable {

}
