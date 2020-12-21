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
    var inVehicleType: String,
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

data class reqProfileSetupResponse(
    var result: String,
    var errorMsg: String? = null
)

data class reqSendVehicleListSearch(
    var vehicleNumber: String
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