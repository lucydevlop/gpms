package io.glnt.gpms.model.enums

enum class ResultType(val code: String, val desc: String) {
    SUCCESS("SUCCESS", "성공"),
    FAILURE("FAILURE", "실패"),
    WAIT("FAILURE", "실패"),
    ERROR("FAILURE", "dop"),
    CANCEL("CANCEL", "취소");

    companion object {
        fun code(s: String): TicketType? = TicketType.values().find { it.code == s }
    }
}