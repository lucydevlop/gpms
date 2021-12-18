package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.vladmihalcea.hibernate.type.json.JsonStringType
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.common.utils.JsonToMapConverter
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.*
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(name="tb_ticketclass")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDef(name = "json", typeClass = JsonStringType::class)
data class TicketClass(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "ticket_name", nullable = false)
    var ticketName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = false)
    var ticketType: TicketType? = TicketType.SEASONTICKET,

    @Enumerated(EnumType.STRING)
    @Column(name = "aply_type", nullable = false)
    var aplyType: TicketAplyType? = TicketAplyType.FULL,

    @Column(name = "start_time")
    var startTime: String? = "0000",

    @Column(name = "end_time")
    var endTime: String? = "2400",

    @Enumerated(EnumType.STRING)
    @Column(name = "range_type")
    var rangeType: DiscountRangeType? = DiscountRangeType.ALL,

    @Column(name = "effect_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate),

    @Column(name = "expire_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),

    @Column(name = "price")
    var price: Int? = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = true)
    var vehicleType: VehicleType? = VehicleType.SMALL,

    @Column(name = "available")
    var available: Int? = 0, //구매 이후 1년 사용 가능(시간권일 경우)

    @Type(type = "json")
    @Column(name = "period", columnDefinition = "json")
    @Convert(attributeName = "period", converter = JsonToMapConverter::class)
    var period: Map<String, Any>? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "extend_yn", nullable = true)
    var extendYn: Yn? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N

): Auditable(), Serializable {

}
