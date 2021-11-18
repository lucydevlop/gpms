package io.glnt.gpms.model.dto.external

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class ReqEnterNotiDTO(
    var vehicleNo: String? = null,

    @get: NotNull
    @JsonFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var inDate: LocalDateTime? = null,

    @get: NotNull
    var gateId: String? = null,

    var gateName: String? = null
)
