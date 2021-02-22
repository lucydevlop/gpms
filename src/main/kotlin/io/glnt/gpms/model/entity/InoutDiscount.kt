package io.glnt.gpms.io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_inout_discount")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class InoutDiscount(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sn", unique = true, nullable = false)
    var sn: Long?,

    @Column(name = "discount_type", nullable = false)
    var discontType: String,

    @Column(name = "ticket_sn", nullable = true)
    var ticketSn: Long? = null,

    @Column(name = "in_sn", nullable = false)
    var inSn: Long,

    @Column(name = "quantity", nullable = false)
    var quantity: Int? = 1

): Auditable(), Serializable {

}
