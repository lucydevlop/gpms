package io.glnt.gpms.handler.tmap.model

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
