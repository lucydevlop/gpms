package io.glnt.gpms.model.dto

data class CorpVisitorRegisterDTO(
    val enabled: Boolean = false
) {
    fun init() : CorpVisitorRegisterDTO = CorpVisitorRegisterDTO(
        enabled = false
    )

    override fun toString() = "{" +
            "\"enabled\": false(true-활성/false-비활성)" +
            "}"
}