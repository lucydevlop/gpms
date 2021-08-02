package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.enums.DelYn
import org.hibernate.annotations.Where
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_barcode_class")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BarcodeClass(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Enumerated(EnumType.STRING)
    @Column(name = "del_yn", nullable = true)
    var delYn: DelYn? = DelYn.N,

    @Column(name = "start")
    var start: Int? = null,

    @Column(name = "end")
    var end: Int? = null,

    @Column(name = "discount_class_sn", nullable = false)
    var discountClassSn: Long,
): Auditable(), Serializable {

    @OneToOne
    @JoinColumn(name = "discount_class_sn", referencedColumnName = "sn", insertable = false, updatable = false)
    @Where(clause = "del_yn = 'N'")
    var discountClass: DiscountClass? = null
}
