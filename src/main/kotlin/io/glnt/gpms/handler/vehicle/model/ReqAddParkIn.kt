package io.glnt.gpms.handler.vehicle.model

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDateTime

data class reqAddParkIn(
    /* 입차 차량 연계 시 필수 param */
    var vehicleNo: String,
    var facilitiesId: String,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT" )
    var inDate: LocalDateTime,
    var resultcode: String,
    /* 차량이미지 File (base64) */
    var base64Str: String? = null,
    var originFileName: String? = null,


    /* 연계 필요한 항목들 정의 */
    var parkingtype: String? = "미인식차량",
    var validDate: LocalDateTime? = null,
    var requestId: String? = null,
    var groupNum: Int? = null,  //삭제 처리 예정(gate Id로 대체)
    var fileName: String? = null,
    var fileUploadId: String? = null,
    var fileFullPath: String? = null,
    var recognitionResult: String? = null
)
