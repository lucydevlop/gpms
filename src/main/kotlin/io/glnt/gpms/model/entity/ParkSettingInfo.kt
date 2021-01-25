package io.glnt.gpms.model.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import io.glnt.gpms.model.entity.Auditable
import io.glnt.gpms.model.enums.checkUseStatus
import java.io.Serializable
import javax.persistence.*

@Entity
@Table(schema = "glnt_parking", name="tb_park_alarm_setting")
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ParkAlarmSetting(
    @Id
    @Column(name = "siteid", unique = true, nullable = false)
    var siteid: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "pay_alarm")
    var payAlarm: checkUseStatus? = checkUseStatus.N,

    @Column(name = "pay_limit_time")
    var payLimitTime: Int? = 0

): Auditable(), Serializable {

}
