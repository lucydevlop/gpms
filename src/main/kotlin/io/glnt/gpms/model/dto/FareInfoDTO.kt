package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.FareInfo
import io.glnt.gpms.model.entity.FarePolicy
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.FareType
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class FareInfoDTO(
    var sn: Long?,

    @get: NotNull
    var fareName: String? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var expireDate: LocalDateTime? = null,

    @Enumerated(EnumType.STRING)
    @get: NotNull
    var type: FareType? = null,

    var time1: Int? = null,

    var won1: Int? = null,

    var count1: Int? = 1,

    var time2: Int? = null,

    var won2: Int? = null,

    var count2: Int? = 1,

    var time3: Int? = null,

    var won3: Int? = null,

    var count3: Int? = 1,

    var count: Int? = 1,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: DelYn? = null
): Serializable {

    constructor(fareInfo: FareInfo) :
        this(
            fareInfo.sn, fareInfo.fareName, fareInfo.effectDate, fareInfo.expireDate, fareInfo.type,
            fareInfo.time1, fareInfo.won1, fareInfo.count1,
            fareInfo.time2, fareInfo.won2, fareInfo.count2,
            fareInfo.time3, fareInfo.won3, fareInfo.count3, fareInfo.count, fareInfo.delYn
        )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FareInfoDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31

}
