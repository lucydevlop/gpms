package io.glnt.gpms.model.dto

data class InoutNotiDTO(
    val url: String? = null,
    val token: String? = null,
) {
    fun init(): InoutNotiDTO = InoutNotiDTO(
        url = "http://localhost/test",
        token = "eyJhbGciOiJIUzI1NiIsI",
    )

    override fun toString()= "{" +
            "\"url\": \"site url\"," +
            "\"token\": \"인증토근값(불필요시 미입력 가능)\"," +
            "}"

}
