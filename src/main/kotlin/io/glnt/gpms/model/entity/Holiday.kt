package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.HolidayType
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_holiday")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class Holiday(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "name")
    var name: String? = null,

    @Column(name = "start_date", nullable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var startDate: LocalDateTime,

    @Column(name = "end_date", nullable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var endDate: LocalDateTime,

    @Column(name = "start_time", length=10)
    var startTime: String? = "0000",

    @Column(name = "end_time", length=10)
    var endTime: String? = "2359",

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    var type: HolidayType? = HolidayType.HOLIDAY,

    @Column(name = "is_working")
    var isWorking: Boolean? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: YN? = YN.N

): Auditable(), Serializable {

    override fun hashCode(): Int {
        var result = sn.hashCode()
        result = 31 * result + startDate.hashCode()
        result = 31 * result + endDate.hashCode()
        return result
    }
}
