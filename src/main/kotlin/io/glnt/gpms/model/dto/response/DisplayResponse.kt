package io.glnt.gpms.model.dto.response

import io.glnt.gpms.model.dto.entity.DisplayColorDTO
import io.glnt.gpms.model.dto.entity.DisplayMessageDTO
import io.glnt.gpms.model.entity.DisplayInfo

data class DisplayResponse(
    var messages: List<DisplayMessageDTO>,
    var info: DisplayInfo,
    var colors: List<DisplayColorDTO>
)
