package io.glnt.gpms.model.dto

data class EmergencyCarDTO(
    val numbers: MutableSet<String>? = null
) {
    fun init() : EmergencyCarDTO = EmergencyCarDTO(
        numbers = mutableSetOf("999", "998")
    )

    override fun toString() = "{" +
            "\"numbers\": [\"차량앞3자리번호1\", \"차량앞3자리번호2\"]" +
            "}"
}
