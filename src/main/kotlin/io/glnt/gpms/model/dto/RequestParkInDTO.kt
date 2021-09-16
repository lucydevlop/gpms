package io.glnt.gpms.model.dto

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime
import javax.validation.constraints.NotNull

data class RequestParkInDTO(
    /** INPUT START **/
    @get: NotNull
    var vehicleNo: String? = null,

    @get: NotNull
    var dtFacilitiesId: String? = null,

    @get: NotNull
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
    var date: LocalDateTime? =  null,

    @get: NotNull
    var resultcode: String? = null,

    /* 차량이미지 File (base64) */
    var base64Str: String? = null,

    @get: NotNull
    var uuid: String? = null,
    /** INPUT END **/

    var fileFullPath: String? = null,
    var fileName: String? = null,
    var fileUploadId: String? = null
)
