package io.glnt.gpms.model.dto.entity

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.entity.CgBasic
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import java.time.LocalDateTime

data class CgBasicDTO (
    var sn: Long?,

    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var effectDate: LocalDateTime? = null,

    var serviceTime: Int? = null,

    var regTime: Int? = null,

    var ticketTime: Int? = null,

    var dayMaxAmt: Int? = null,

    var delYn: YN? = null

): Serializable {

    constructor(cgBasic: CgBasic) :
            this(
                cgBasic.sn, cgBasic.effectDate, cgBasic.serviceTime, cgBasic.regTime, cgBasic.ticketTime, cgBasic.dayMaxAmt, cgBasic.delYn
            )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CgBasicDTO) return false
        return sn != null && sn == other.sn
    }

    override fun hashCode() = 31
}