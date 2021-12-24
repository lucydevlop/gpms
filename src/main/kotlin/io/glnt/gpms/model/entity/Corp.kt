package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import org.hibernate.annotations.Cache
import org.hibernate.annotations.CacheConcurrencyStrategy
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_corp",
    indexes = [Index(columnList = "corpId", unique = true)])
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
data class Corp(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true)
    var sn: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn")
    var delYn: YN? = null,

//    @Column(name = "flag", nullable = false)
//    var flag: Int = 1,

//    @GenericGenerator(
//            name = "inovices",
//            strategy = "io.glnt.gpms.common.utils.StringPrefixedSequenceIdGenerator"
////            parameters = [
////                    Parameter(name = StringPrefixedSequenceIdGenerator.INCREMENT_PARAM, value = "50"),
////                    Parameter(name = StringPrefixedSequenceIdGenerator.VALUE_PREFIX_PARAMETER, value = "B_"),
////                    Parameter(name = StringPrefixedSequenceIdGenerator.NUMBER_FORMAT_PARAMETER, value = "%05d")
////            ]
//    )
    @Column(name = "corpId")
    var corpId: String? = null,

//    @Column(name = "password", nullable = false)
//    var password: String,

    @Column(name = "corpName", unique = true)
    var corpName: String? = null,

//    @Column(name = "login_date")
//    var loginDate: LocalDateTime? = null,
//
//    @Column(name = "passwd_chage_date")
//    var passwdChangeDate: LocalDateTime? = null,

    @Column(name = "form")
    var form: Int? = null,

    @Column(name = "resident")
    var resident: Int? = null,

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Corp) return false
        if (other.sn == null || sn == null) return false

        return Objects.equals(sn, other.sn)
    }

    override fun hashCode(): Int {
        return 31
    }

    companion object {
        private const val serialVersionUID = 1L
    }

}
