package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_seasonticket")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProductTicket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "corp_sn", nullable = true)
    var corpSn: Int? = null,

    @Column(name = "corp_name", nullable = true)
    var corpName: String? = null,

    @Column(name = "dept_name", nullable = true)
    var deptName: String? = null,

    @Column(name = "vehicleNo", nullable = false)
    var vehicleNo: String,

    @Column(name = "ticket_fee", nullable = true)
    var ticketFee: Int? = null,

    @Column(name = "valid_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var validDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59"),

    @Column(name = "flag", nullable = true)
    var flag: Int? = null,

    @Column(name = "cardnum", nullable = true)
    var cardNum: String? = null,

    @Column(name = "color", nullable = true)
    var color: String? = null,

    @Column(name = "vehiclekind", nullable = true)
    var vehiclekind: String? = null,

    @Column(name = "name", nullable = true)
    var name: String? = null,

    @Column(name = "tel", nullable = true)
    var tel: String? = null,

    @Column(name = "jikchk", nullable = true)
    var jikchk: String? = null,

    @Column(name = "etc", nullable = true)
    var etc: String? = null,

    @Column(name = "etc1", nullable = true)
    var etc1: String? = null,

    @Column(name = "userId", nullable = true)
    var userId: String? = null,

    @Column(name = "chargerId", nullable = true)
    var chargerId: String? = null,

    @Column(name = "chargertel", nullable = true)
    var chargertel: String? = null,

    @Column(name = "reg_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var regDate: LocalDateTime? = null
): Auditable(), Serializable {

}
