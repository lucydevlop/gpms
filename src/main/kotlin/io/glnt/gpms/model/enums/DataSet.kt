package io.glnt.gpms.model.enums

enum class checkUseStatus {
    Y, N
}

enum class GateTypeStatus {
    IN, OUT, IN_OUT
}

enum class LprTypeStatus {
    FRONT, BACK, ASSIST
}

enum class DisplayPosition(val code: String, val desc: String) {
    IN("IN", "입구"),
    OUT("OUT", "출구")
}

enum class DisplayType(val code: String, val desc: String) {
    NORMAL1("NORMAL1", "일반(첫번째줄)"),
    NORMAL2("NORMAL2", "일반(두번째줄)"),
    EMPHASIS("EMPHASIS", "강조"),


}
//enum class parkCarType {
//    "일반차량", "정기권차량", "미인식차량"
//}