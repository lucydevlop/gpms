package io.glnt.gpms.handler.inout.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.model.enums.DisplayMessageClass
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqAddParkIn(
    /* 입차 차량 연계 시 필수 param */
    var vehicleNo: String,
    var facilitiesId: String,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
    var inDate: LocalDateTime,
    var resultcode: String,
    /* 차량이미지 File (base64) */
    var base64Str: String? = null,
    var originFileName: String? = null,
    var uuid: String,


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

data class reqAddParkOut(
    /* 입차 차량 연계 시 필수 param */
    var vehicleNo: String,
    var facilitiesId: String,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
    var outDate: LocalDateTime,
    var resultcode: String,
    /* 차량이미지 File (base64) */
    var base64Str: String? = null,
    var originFileName: String? = null,
    var uuid: String,

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

data class reqSearchParkin(
    var vehicleNo: String? = null,
    @Enumerated(EnumType.STRING) var type: DisplayMessageClass? = null,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,
    var pageSize: Long? = 10,
    var page: Int? = 1,
    var gateId: String? = null
)

data class reqUpdatePayment(
    var approveDateTime: String? = null,
    var cardNumber: String? = null,
    var parkcarType: String? = null,
    var cardtransactionId: String? = null,
    var paymentAmount: Int? = null,
    var parkTicketAmount: Int? = null,
    var sn: Long
)