package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_cgbasic")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class CgBasic(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "serviceTime", nullable = true)
    var serviceTime: Int? = 0,

    @Column(name = "regTime", nullable = true)
    var regTime: Int? = 0,

    @Column(name = "ticket_time", nullable = true)
    var ticketTime: Int? = 0,

    @Column(name = "residentDiscount", nullable = true)
    var residentDiscount: Int? = 0,

    @Column(name = "day_max_amt", nullable = true)
    var dayMaxAmt: Int? = 0,

    @Column(name = "effect_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {

}
