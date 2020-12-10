package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_corp",
    indexes = [Index(columnList = "corpId", unique = true)])
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Corp(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "flag", nullable = false)
    var flag: Int = 1,

    @Column(name = "corpId", nullable = false)
    var corpId: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "corpName", nullable = false)
    var corpName: String,

    @Column(name = "login_date")
    var loginDate: LocalDateTime? = null,

    @Column(name = "passwd_chage_date")
    var passwdChangeDate: LocalDateTime? = null,

    @Column(name = "form", nullable = false)
    var form: Int = 1,

    @Column(name = "resident", nullable = false)
    var resident: Int = 1,

    @Column(name = "dong", nullable = true)
    var dong: String? = null,

    @Column(name = "ho", nullable = true)
    var ho: String? = null,

    @Column(name = "ceoName", nullable = true)
    var ceoName: String? = null,

    @Column(name = "tel", nullable = true)
    var tel: String? = null,

    @Column(name = "mobile", nullable = true)
    var mobile: String? = null,

    @Column(name = "email", nullable = true)
    var email: String? = null,

    @Column(name = "address", nullable = true)
    var address: String? = null,

    @Column(name = "saupNo", nullable = true)
    var saupNo: String? = null,

    @Column(name = "mobileNo", nullable = true)
    var mobileNo: String? = null,

    @Column(name = "balance", nullable = true)
    var balance: Int? = null,

    @Column(name = "last_charging", nullable = true)
    var lastCharging: Long? = null,

    @Column(name = "last_discount", nullable = true)
    var lastDiscount: Long? = null,

    @Column(name = "balance_update", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var balanceUpdate: LocalDateTime? = null

) : Auditable(), Serializable {

}
