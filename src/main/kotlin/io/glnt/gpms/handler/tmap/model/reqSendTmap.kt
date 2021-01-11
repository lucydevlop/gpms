package io.glnt.gpms.handler.tmap.model

data class reqApiTmapCommon (
    var type: String? = null,
    var parkingSiteId: String? = null,
    var requestId: String? = null,
    var responseId: String? = null,
    var eventDateTime: String? = null,
    var commandDateTime: String? = null,
    var contents: Any
)

data class reqTmapFacilitiesList (
    var map: ArrayList<parkinglotMap> = ArrayList(),
    var gateList: ArrayList<gateLists> = ArrayList(),
    var facilitiesList: ArrayList<facilitiesLists> = ArrayList()
)

data class reqTmapInVehicle(
    var gateId: String,
    var sessionId: String? = null,
    var inVehicleType: String? = null,
    var vehicleNumber: String,
    var recognitionType: String,
    var recognitorResult: String,
    var fileUploadId: String
)

data class reqTmapFileUpload(
    var type: String,
    var parkingSiteId: String? = null,
    var eventType: String,
    var requestId: String,
    var fileUploadId: String,
    var fileName: String,
    var fileUploadDateTime: String
)

data class reqTmapFailureAlarm(
    var facilitiesId: String,
    var failureAlarm: String?
)

data class reqFacilitiesRegist(
    var fileUploadId: String
)

data class reqOutVehicle(
    var gateId: String,
    var seasonTicketYn: String,
    var vehicleNumber: String,
    var recognitionType: String,
    var recognitorResult: String,
    var fileUploadId: String

)

data class reqAdjustmentRequest(
    var vehicleNumber: String,
    var paymentMachineType: String,
    var gateId: String,
    var facilitiesId: String,
    var recognitionType: String,
    var fileuploadId: String
)

data class reqSendResultResponse(
    var result: String,
    var errorMsg: String? = null
)

data class reqSendVehicleListSearch(
    var vehicleNumber: String,
    var facilityId: String?
)

data class reqSendPayment(
    var gateId: String? = null,
    var facilitiesId: String? = null,
    var vehicleNumber: String,
    var chargingId: String,
    var paymentMachineType: String,
    var transactionId: String,
    var paymentType: String,
    var paymentAmount: Int,
    var businessOwnerName: String? = null,
    var cardCompanyName: String? = null,
    var cardNumber: String? = null,
    var cardApprovalNumber: String? = null,
    var tributaryDiscountList: ArrayList<tributaryDiscountItem>? = null
)

data class tributaryDiscountItem(
    var tributaryDiscountTicketId: String? = null,
    var tributaryDiscountTicketName: String? = null,
    var tributaryDiscountTicketApplyDateTime: String? = null,
    var discountAmount: Int? = null
)

data class parkinglotMap(
    var floor: ArrayList<floorMap> = ArrayList(),
    var zone: ArrayList<zoneMap> = ArrayList(),
    var parkingSpot: ArrayList<parkingSpotMap> = ArrayList()
)

data class floorMap (
    var floorId: String,
    var floorName: String,
    var image: imageMap?
)

data class imageMap (
    var id: String,
    var fileName: String?,
    var reoulution: String?
)

data class zoneMap (
    var zoneId: String,
    var zoneName: String,
    var floorId: String?,
    var position: ArrayList<positionMap>? = null
)

data class positionMap(
    var xy : ArrayList<Int>,
    var size: ArrayList<Int>,
    var unit: String,
    var type: String
)

data class parkingSpotMap(
    var parkingSpotId: String,
    var parkingSpotName: String,
    var parkingSpotType: String,
    var floorId: String,
    var zoneId: String? = null,
    var position: ArrayList<positionMap>? = null
)

data class gateLists(
    var dtGateId: String,
    var gateName: String,
    var gateType: String,
    var dtFacilitiesId: Array<String>,
    var position: ArrayList<positionMap>? = null
)

data class facilitiesLists(
    var category: String,
    var modelId: String,
    var dtFacilitiesId: String,
    var facilitiesName: String,
    var host: String? = null,
    var port: String? = null,
    var channel: String? = null,
    var nvr: String? = null,
    var filmingParkingSpotId: ArrayList<String>? = null,
    var filmingZoneId: ArrayList<String>? = null,
    var parkingSpotId: ArrayList<String>? = null,
    var zoneId: ArrayList<String>? = null,
    var leftParkingSpotId: ArrayList<String>? = null,
    var leftZoneId: ArrayList<String>? = null,
    var centerParkingSpotId: ArrayList<String>? = null,
    var centerZoneId: ArrayList<String>? = null,
    var rightParkingSpotId: ArrayList<String>? = null,
    var rightZoneId: ArrayList<String>? = null,
    var position: ArrayList<positionMap>? = null
)