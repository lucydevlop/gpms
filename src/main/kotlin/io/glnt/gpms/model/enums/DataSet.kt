package io.glnt.gpms.model.enums

enum class checkUseStatus {
    Y, N
}

enum class DelYn {
    Y, N
}

enum class Yn {
    Y, N
}

enum class GateTypeStatus {
    IN, OUT, IN_OUT, ETC
}

enum class LprTypeStatus {
    FRONT, BACK, ASSIST, INFRONT, INBACK, OUTFRONT
}

enum class OnOff {
    ON, OFF
}

enum class ResultType {
    SUCCESS, FAILURE, WAIT, ERROR, CANCEL
}

enum class FacilityCategoryType {
    LPR, BREAKER, DISPLAY, PAYSTATION, VOIP
}

enum class DiscountApplyTargetType {
    IN, NOW
}

enum class DisplayMessageClass(val code: String, val desc: String) {
    IN("IN", "입차"),
    OUT("OUT", "출차"),
    WAIT("WAIT", "정산대기"),
}

enum class DisplayMessageType(val code: String, val desc: String) {
    INIT("INIT", "RESET"),
    MEMBER("MEMBER", "Tmap(회원)차량"),
    NONMEMBER("NONMEMBER", "일반차량"),
    VIP("VIP", "정기권차량"),
    CALL("CALL", "호출"),
    FAIL("FAIL", "실패"),
    FAILNUMBER("FAILNUMBER", "미인식차량"),
    RESTRICTE("RESTRICTE", "입차제한차량"),
    FULL("FULL", "만차제한"),
    ERROR("ERROR", "시설물에러")
}

enum class DisplayMessageCode(val code: String, val desc: String) {
    DESC("DESC", "메세지내용"),
    PAYMENT("PAYMENT", "결제금액"),
    CARNUM("CARNUM", "차량번호"),
    REMAINDAYS("REMAINDAYS", "정기권남은일자"),
    FAIL("FAIL", "실패"),
}

enum class SetupOption {
    ADD, UPDATE, DELETE, OVERWRITE
}

enum class TicketType(val code: String, val desc: String) {
    SEASONTICKET("SEASONTICKET", "정기권"),
    WHITELIST("WHITELIST", "화이트리스트"),
    VISITTICKET("VISITTICKET", "방문권"),
    TIMETICKET("TIMETICKET", "시간권"),
    DAYTICKET("DAYTICKET", "일일권"),
    FREETICKET("FREETICKET", "무료주차권"),
    CORPTICKET("CORPTICKET", "입주사할인권"),
    DISCOUNT("DISCOUNT", "할인"),
    BARCODE("BARCODE", "바코드할인"),
    ALL("ALL", "전체할인권"),
    ETC("ETC", "기타");

    companion object {
        fun from(s: String): TicketType? = values().find { it.desc == s }
    }
}

enum class DiscountRangeType(val code: String, val desc: String) {
    WEEKDAY("WEEKDAY", "평일"),
    WEEKEND("WEEKEND", "주말"),
    ALL("ALL", "전체")
}

enum class SaleType(val code: String, val desc: String) {
    PAID("PAID", "유료권"),
    FREE("FREE", "무료권")
}

enum class VehicleType(val code: String, val desc: String) {
    SMALL("SMALL", "소형"),
    MEDIUM("MEDIUM", "중형"),
    LARGE("LARGE", "중형")
}

enum class WeekType(val code: Int, val desc: String) {
    SUN(1, "일요일"),
    MON(2, "월요일"),
    TUE(3, "화요일"),
    WED(4, "수요일"),
    THU(5, "목요일"),
    FRI(6, "금요일"),
    SAT(7, "토요일"),
    ALL(0, "ALL")
}

enum class FareType(val code: String, val desc: String) {
    BASIC("BASIC", "기본요금"),
    ADD("ADD", "추가요금")
}

enum class HolidayType(val code: String, val desc: String) {
    HOLIDAY("HOLIDAY", "휴일"),
    SPECIALDAY("SPECIALDAY", "특근일"),
    ETC("ETC", "기타")
}

enum class TimeTarget(val code: String, val desc: String) {
    IN("IN", "입차"),
    NOW("NOW", "현재")
}

enum class OpenActionType(val code: String, val desc: String) {
    NONE("NONE", "제한없음"),
    RECOGNITION("RECOGNITION", "인식"),
    RESTRICT("RESTRICT", "제한"),
    MULTI("MULTI", "복합")
}

enum class DiscountApplyType(val code: String, val desc: String) {
    TIME("TIME", "시간"),
    WON("WON", "금액"),
    PERCENT("PERCENT", "%")
}

enum class DiscountApplyRateType {
    FIX, VARIABLE
}

enum class DiscountType {
    DISCOUNT, REDUCTION
}

enum class ErrorCode {
    NOT_FOUND,
    ALREADY_EXISTS,
    ACCESS_DENIED,
    INCORRECT_VALUE
}

enum class DateType(val code: String, val desc: String) {
    EFFECT("EFFECT", "시작일"),
    EXPIRE("EXPIRE", "종료일"),
    VALIDATE("VALIDATE", "사용가능")
}

enum class DisplayStatus(val code: String, val desc: String) {
    FLOW("FLOW", "흐름"),
    FIX("FIX", "고정")
}

enum class VehicleDayType(val code: String, val desc: String) {
    OFF("OFF", "OFF"),
    DAY2("DAY2", "2부제"),
    DAY5("DAY5", "5부제")
}

enum class PayType(val code: String, val desc: String) {
    CARD("CARD", "카드"),
    CASH("CASH", "현금")
}

enum class ExternalSvrType(val code: String, val desc: String) {
    NONE("NONE", "없음"),
    TMAP("TMAP", "티맵연동"),
    ADTCAPS("ADTCAPS", "ADT캡스"),
    GLNT("GLNT", "지엘엔티")
}

enum class CityType {
    SEOUL, GYEONGGIDO, INCHEON, BUSAN, DAEJEON, DAEGU, ULSAN, SEJONG, GWANGJU, GANGWONDO, CHUNGCHEONGBUKDO, CHUNGCHEONGNAMDO, GYEONGSANGBUKDO, GYEONGSANGNAMDO, JEOLLABUKDO, JEOLLANAMDO, JEJU
}

enum class TicketAplyType {
    FULL, DAY, NIGHT, TIME
}

enum class VisitorExternalKeyType {
    APTNER
}

enum class OperatingDayType {}

enum class DiscountApplyCriteriaType {
    FRONT, BACK
}
//enum class parkCarType {
//    "일반차량", "정기권차량", "미인식차량"
//}