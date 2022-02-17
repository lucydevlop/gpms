package io.glnt.gpms.model.enums

enum class SettingKey(val code: String, val desc: String) {
    VISITOR_LINK("VISITOR_LINK", "방문자 외부 연계"),
    INOUT_NOTI("INOUT_NOTI", "입출차 외부 연계"),
    EMERGENCY_CAR_ENTER("EMERGENCY_CAR_ENTER", "긴급 자동차 출입"),
    CORP_VISITOR_REGISTER("CORP_VISITOR_REGISTER","입주사 방문권 등록")
}