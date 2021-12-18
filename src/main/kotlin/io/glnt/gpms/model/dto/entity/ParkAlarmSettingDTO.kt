package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.ParkAlarmSetting
import io.glnt.gpms.model.enums.checkUseStatus
import java.io.Serializable
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class ParkAlarmSettingDTO (
    var siteid: String? = null,

    @Enumerated(EnumType.STRING)
    var payAlarm: checkUseStatus? = checkUseStatus.N,

    var payLimitTime: Int? = 0,

    @Enumerated(EnumType.STRING)
    var gateAlarm: checkUseStatus? = checkUseStatus.N,

    var gateLimitTime: Int? = 0,

    var gateCounterResetTime: Int? = 0

): Serializable {
    constructor(parkAlarmSetting: ParkAlarmSetting) :
        this(
            parkAlarmSetting.siteid, parkAlarmSetting.payAlarm, parkAlarmSetting.payLimitTime,
            parkAlarmSetting.gateAlarm, parkAlarmSetting.gateLimitTime, parkAlarmSetting.gateCounterResetTime
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ParkAlarmSettingDTO) return false
        return siteid != null && siteid == other.siteid
    }

    override fun hashCode() = 31
}