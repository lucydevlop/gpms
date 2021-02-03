package io.glnt.gpms.model.entity

//import androidx.room.*
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.squareup.moshi.Json
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.TicketType
import io.glnt.gpms.model.enums.VehicleType
import org.hibernate.annotations.Type
import org.hibernate.annotations.TypeDef
import org.springframework.format.annotation.DateTimeFormat
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_seasonticket")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@TypeDef(name = "json", typeClass = JsonStringType::class)
data class ProductTicket(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "corp_sn", nullable = true)
    var corpSn: Long? = null,

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
    var validDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = false)
    var delYn: DelYn? = DelYn.N,

    @Column(name = "cardnum", nullable = true)
    var cardNum: String? = null,

    @Column(name = "color", nullable = true)
    var color: String? = null,

    @Column(name = "vehiclekind", nullable = true)
    var vehiclekind: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "vehicle_type", nullable = true)
    var vehicleType: VehicleType? = VehicleType.MEDIUM,

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
    var regDate: LocalDateTime? = null,

    @Column(name = "effect_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = DateUtil.beginTimeToLocalDateTime(DateUtil.nowDate),

    @Column(name = "expire_date", nullable = true)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = DateUtil.stringToLocalDateTime("9999-12-31 23:59:59", "yyyy-MM-dd HH:mm:ss"),

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_type", nullable = true)
    var ticketType: TicketType? = TicketType.SEASONTICKET,

    @Type(type = "json")
    @Column(name = "gates", columnDefinition = "json")
//    @Json(name = "gate_id")
//    @Column(name = "gate_id")
//    @TypeConverters(ListConverter::class)
    var gates: MutableSet<String>? = mutableSetOf() // mutableListOf("ALL")
): Auditable(), Serializable {

}

//class ListConverter{
//    private val moshi = Moshi.Builder().build()
//    private val type = Types.newParameterizedType(List::class.java, String::class.java)
//    private val listAdapter: JsonAdapter<List<String>> = moshi.adapter(type)
//
//    @TypeConverter
//    fun fromList(list: List<String>?) : String? {
//        return listAdapter.toJson(list)
//    }
//    @TypeConverter
//    fun toList(jsonList: String?): List<String>? {
//        return jsonList?.let { listAdapter.fromJson(jsonList) }
//    }
//}


//class StringListTypeConverter {
//    val moshi = Moshi.Builder().build()
//
//    val stringTypeAdapter: JsonAdapter<List<String>> = moshi.adapter(
//        Types.newParameterizedType(
//            MutableList::class.java,
//            String::class.java
//        )
//    )
//
//    @TypeConverter
//    fun convertListTo(types: List<String>): String {
//        return stringTypeAdapter.toJson(types)
//    }
//
//    @TypeConverter
//    fun convertStringTo(json: String): List<String>? {
//        return stringTypeAdapter.fromJson(json)
//    }
//}