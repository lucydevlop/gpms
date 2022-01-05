package io.glnt.gpms.handler.inout.model

import com.fasterxml.jackson.annotation.JsonFormat
import io.glnt.gpms.handler.calc.model.BasicPrice
import io.glnt.gpms.model.dto.entity.ParkInDTO
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.entity.ParkOut
import io.glnt.gpms.model.enums.DisplayMessageClass
import java.time.LocalDate
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated

data class reqAddParkIn(
    /* 입차 차량 연계 시 필수 param */
    var vehicleNo: String,
    var dtFacilitiesId: String,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
    var date: LocalDateTime,
    var resultcode: String,
    /* 차량이미지 File (base64) */
    var base64Str: String? = null,
    var originFileName: String? = null,
    var uuid: String? = null,
    var memo: String? = null,

    /* update 사용 */
    var inSn: Long? = null,
    var ticketSn: Long? = null,

    /* 연계 필요한 항목들 정의 */
    var parkingtype: String? = "미인식차량",
    var validDate: LocalDateTime? = null,
    var requestId: String? = null,
    var groupNum: Int? = null,  //삭제 처리 예정(gate Id로 대체)
    var fileName: String? = null,
    var fileUploadId: String? = null,
    var fileFullPath: String? = null,
    var recognitionResult: String? = null,

    var isSecond: Boolean? = false,
    var isEmergency: Boolean? = null,

    var beforeParkIn: ParkInDTO? = null
)

data class reqAddParkOut(
    /* 입차 차량 연계 시 필수 param */
    var vehicleNo: String,
    var dtFacilitiesId: String,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd HH:mm:ss")
    var date: LocalDateTime,
    var resultcode: String,
    /* 차량이미지 File (base64) */
    var base64Str: String? = null,
    var originFileName: String? = null,
    var uuid: String,

    /* update */
    var outSn: Long? = null,

    /* 연계 필요한 항목들 정의 */
    var parkingtype: String? = "미인식차량",
    var validDate: LocalDateTime? = null,
    var requestId: String? = null,
    var groupNum: Int? = null,  //삭제 처리 예정(gate Id로 대체)
    var fileName: String? = null,
    var fileUploadId: String? = null,
    var fileFullPath: String? = null,
    var recognitionResult: String? = null,
    var parkIn: ParkIn? = null,
    var parkOut: ParkOut? = null,
    var price: BasicPrice? = null,
    var deviceIF: String? = "ON"
)

data class reqSearchParkin(
    var searchLabel: String? = null,
    var searchText: String? = null,
    @Enumerated(EnumType.STRING) var searchDateLabel: DisplayMessageClass? = null,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var fromDate: LocalDate? = null,
    @JsonFormat( shape = JsonFormat.Shape.ANY, pattern = "yyyy-MM-dd") var toDate: LocalDate? = null,
//    var pageSize: Long? = 10,
//    var page: Int? = 1,
    var gateId: String? = null,
    var parkcartype: String? = null,
    var outSn: Long? = null
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


data class reqVisitorExternal(
    var kaptCode: String? = null,
    var carNo: String? = null,
    var dong: String? = null,
    var ho: String? = null,
    var isResident: String? = null
)