package io.glnt.gpms.model.dto

data class VisitorLinkDTO (
    val site: String? = null,
    val url: String? = null,
    val token: String? = null,
    val key: String? = null
) {
    fun init(): VisitorLinkDTO = VisitorLinkDTO(
        site = "APTNER",
        url = "https://devgtw.aptner.com/pc",
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJBUFRORVIiLCJhdWQiOiJHTFx1MDAyNlQiLCJleHAiOjMxODEyNjQ3MDMsImlhdCI6MTYyNjA2NDcwMywicm9sZXMiOiJQQyJ9.5zBu-c40X6nbW_ay-PZPjVQGln636AjpSywwtACicyk",
        key = "T77777777"
    )

    override fun toString() = "{" +
            "\"site\": \"방문자 연계 site code(ex.아파트너(APTNER))\"," +
            "\"url\": \"site url\"," +
            "\"token\": \"인증토근값(불필요시 미입력 가능)\"," +
            "\"key\": \"연계 key 값(ex.주차장 key, 불필요시 미입력 가능)\"" +
            "}"
}
