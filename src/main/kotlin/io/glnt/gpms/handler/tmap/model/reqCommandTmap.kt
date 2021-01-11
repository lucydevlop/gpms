package io.glnt.gpms.handler.tmap.model

import io.glnt.gpms.model.enums.SetupOption

data class reqCommandFacilities(
    var facilitiesId: String,
    var BLOCK: String?,
    var gateId: String?
)

data class reqCommandProfileSetup(
    var parkingSpotStatusnotiCycle: String?=null,
    var facilitiesStatusNotiCycle: String?=null,
    var gateList: ArrayList<gateInfo>?=null,
    var messageList: ArrayList<messageInfo>?=null
)

data class gateInfo(
    var gateId: String,
    var takeAction: String?,
    var seasonTicketTakeAction: String?,
    var whiteListTakeAction: String?
)

data class messageInfo(
    var messageType: String?,
    var message: String?
)

data class reqCommandGateTakeActionSetup(
    var gateList: ArrayList<gateList>
)

data class gateList(
    var gateId: String,
    var takeActionType: String,
    var setupOption: SetupOption,
    var vehicleList: ArrayList<vehicleList>
)

data class vehicleList(
    var vehicleNumber: String,
    var messageType: String,
    var startDateTime: String,
    var endDateTime: String

)

data class reqCommandVehicleListSearchResponse(
    var result: String,
    var error: String? = null,
    var vehicleList: ArrayList<vehicleSearchList>? = null
)

data class vehicleSearchList(
    var vehicleNumber: String,
    var inVehicleDateTime: String,
    var inVehicleImageId: String? = null,
    var parkingLocation: String? = null
)

data class reqInOutVehicleInformationSetup(
    var sessionId: String,
    var vehicleNumber: String,
    var inVehicleDateTime: String,
    var setupOption: SetupOption,
    var informationType: String
)

data class reqAdjustmentRequestResponse(
    var result: String,
    var errorMsg: String? = null,
    var inVehicleDateTime: String,
    var chargingStartDateTime: String,
    var chargingRequestDateTime: String,
    var chargingId: String,
    var chargingTimes: String? = null,
    var sessionId: String? = null,
    var beforehandPaymentAmount: Int? = null,
    var parkingAmount: Int,
    var chargingAmount: Int,
    var adjustmentAmount: Int,
    var basicsChargeCalculationYn: String? = null,
    var outVehicleAllowYn: String,
    var allowYnReason: String,
    var parkingTicketApplyList: ArrayList<parkingTicketApplyList>? = null,
    var discountList: ArrayList<discountList>? = null,
    var holidayList: ArrayList<holidayList>? = null
)

data class parkingTicketApplyList(
    var parkingTicketType: String? = null,
    var applyStartDateTime: String? = null,
    var applyEndDateTime: String? = null
)

data class discountList(
    var discountType: String? = null,
    var discountDescription: String? = null,
    var discountUnit: String? = null,
    var discountAmount: String? = null
)

data class holidayList(
    var holidayDate: String? = null,
    var holidayName: String? = null
)