package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import java.io.Serializable
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(schema = "glnt_parking", name="tb_vehiclelistsearch")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class VehicleListSearch(
    @Id
    @Column(name = "requestId", unique = true, nullable = false)
    var requestId: String?,

    @Column(name = "facilityId")
    var facilityId: String?
): Auditable(), Serializable {

}
