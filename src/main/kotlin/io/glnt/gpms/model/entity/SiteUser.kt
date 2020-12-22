package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.UserRole
import io.glnt.gpms.model.enums.checkUseStatus
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.sql.Date
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="site_user")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class SiteUser(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idx", unique = true, nullable = false)
    var idx: Long? = null,

    @Column(name = "id", unique = true, nullable = false, updatable = false)
    var id: String,

    @Column(name = "password", nullable = false)
    var password: String,

    @Column(name = "user_name", unique = false, nullable = false)
    var userName: String,

    @Column(name = "user_phone", nullable = false)
    var userPhone: String?,

    @Column(name = "user_email", nullable = false)
    var userEmail: String? = null,

    @Column(name = "check_use", nullable = false, columnDefinition = "varchar(1) default 'Y'")
    @Enumerated(value = EnumType.STRING)
    var checkUse: checkUseStatus? = checkUseStatus.Y,

    @Column(name = "wrong_count", nullable = false, columnDefinition = "tinyint(1) default 0")
    var wrongCount: Int? = 0,

    @Column(name = "password_date")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var passwordDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", unique = false, nullable = false)
    var role: UserRole? = UserRole.ADMIN,

    @Column(name = "corp_sn")
    var corpSn: Long? = null
): Auditable(), Serializable {

}

