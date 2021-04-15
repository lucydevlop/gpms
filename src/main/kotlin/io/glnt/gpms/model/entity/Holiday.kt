package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.HolidayType
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDate
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_cgholiday")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Holiday(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "holidate", nullable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd")
    var holidate: LocalDate,

    @Column(name = "start_time", length=10)
    var startTime: String? = "0000",

    @Column(name = "end_time", length=10)
    var endTime: String? = "2400",

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: HolidayType? = HolidayType.HOLIDAY,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {

    override fun hashCode(): Int {
        var result = sn.hashCode()
        result = 31 * result + holidate.hashCode()
        return result
    }
}
