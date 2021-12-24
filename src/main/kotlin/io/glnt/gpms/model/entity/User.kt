package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import java.sql.Date
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="_site_admin",
       indexes = [Index(columnList = "admin_id", unique = true)])
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx", unique = true, nullable = false)
    var idx: Long,

    @Column(name = "admin_id", unique = true, nullable = false, updatable = false)
    var adminId: String,

    @get:JsonIgnore
    @Column(name = "admin_pw", unique = false, nullable = false)
    var adminPw: String,

    @Column(name = "admin_name", unique = false, nullable = false)
    var adminName: String,

    @Column(name = "admin_phone", nullable = false)
    var adminPhone: String,

    @Column(name = "admin_email", nullable = false)
    var adminEmail: String,

    @Column(name = "check_use", nullable = false, columnDefinition = "varchar(1) default 'Y'")
    @Enumerated(value = EnumType.STRING)
    var checkUse: YN,

    @Column(name = "wrong_count", nullable = false, columnDefinition = "tinyint(1) default 0")
    var wrongCount: Int,

    @Column(name = "password_date")
    var passwordDate: Date,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", unique = false, nullable = false)
    var role: UserRole
) : Auditable(), Serializable {

//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (javaClass != other?.javaClass) return false
//
//        other as User
//
//        if (adminId != other.adminId) return false
//
//        return true
//    }
//
//    override fun hashCode(): Int {
//        var result = adminId.hashCode()
//        result = 31 * result + adminId.hashCode()
//        return result
//    }
}
