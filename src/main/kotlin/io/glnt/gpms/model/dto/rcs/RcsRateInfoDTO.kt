package io.glnt.gpms.model.dto.rcs

import io.glnt.gpms.model.dto.CgBasicDTO
import io.glnt.gpms.model.dto.FarePolicyDTO
import io.glnt.gpms.model.entity.CgBasic

data class RcsRateInfoDTO(
    val fareBasic: CgBasicDTO? = null,
    val farePolicies: List<FarePolicyDTO>? = null
)
