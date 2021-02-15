package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.vladmihalcea.hibernate.type.json.JsonStringType
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.FareType
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.model.enums.WeekType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.hibernate.annotations.Where
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_fare_policy")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDef(name = "json", typeClass = JsonStringType::class)
data class FarePolicy(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "fare_name", unique = true, nullable = false)
    var fareName: String,

    @Column(name = "vehicle_type", nullable = false)
    var vehicleType: VehicleType? = VehicleType.SMALL,

    @Column(name = "start_time", nullable = false)
    var startTime: String? = "0000",

    @Column(name = "end_time", nullable = false)
    var endTime: String? = "2359",

    @Column(name = "basic_fare_sn", insertable = true, updatable = true)
    var basicFareSn: Long,

    @Column(name = "add_fare_sn", insertable = true, updatable = true)
    var addFareSn: Long? = null,

    @Column(name = "effect_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @Column(name = "expire_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),


    @Type(type = "json")
    @Enumerated(EnumType.STRING)
    @Column(name = "week", nullable = false, columnDefinition = "json")
    var week: MutableSet<WeekType>? = mutableSetOf(WeekType.ALL),

    @Column(name = "day_max_rate", nullable = true)
    var dayMaxRate: Int? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N
): Auditable(), Serializable {

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "basic_fare_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "type = 'BASIC' and del_yn = 'N'")
    var basicFare: FareInfo? = null

    @OneToOne//(mappedBy = "serviceProduct", cascade = arrayOf(CascadeType.ALL), fetch = FetchType.EAGER)
    @JoinColumn(name = "add_fare_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "type = 'ADD' and del_yn = 'N'")
    var addFare: FareInfo? = null
}

@Entity
@Table(schema = "glnt_parking", name="tb_fare_info")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class FareInfo(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "fare_name", unique = true)
    var fareName: String,

    @Column(name = "effect_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @Column(name = "expire_date", nullable = false)
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: FareType? = FareType.BASIC,

    @Column(name = "time_1", nullable = false)
    var time1: Int? = 30,

    @Column(name = "won_1", nullable = false)
    var won1: Int? = 1000,

    @Column(name = "count_1", nullable = false)
    var count1: Int? = 1,

    @Column(name = "time_2", nullable = true)
    var time2: Int? = 0,

    @Column(name = "won_2", nullable = true)
    var won2: Int? = 0,

    @Column(name = "count_2", nullable = true)
    var count2: Int? = 0,

    @Column(name = "time_3", nullable = true)
    var time3: Int? = 0,

    @Column(name = "won_3", nullable = true)
    var won3: Int? = 0,

    @Column(name = "count_3", nullable = true)
    var count3: Int? = 0,

    @Column(name = "time_4", nullable = true)
    var time4: Int? = 0,

    @Column(name = "won_4", nullable = true)
    var won4: Int? = 0,

    @Column(name = "count_4", nullable = true)
    var count4: Int? = 0,

    @Column(name = "time_5", nullable = true)
    var time5: Int? = 0,

    @Column(name = "won_5", nullable = true)
    var won5: Int? = 0,

    @Column(name = "count_5", nullable = true)
    var count5: Int? = 0,

    @Column(name = "count", nullable = false)
    var count: Int? = 1,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N
): Auditable(), Serializable {

}

