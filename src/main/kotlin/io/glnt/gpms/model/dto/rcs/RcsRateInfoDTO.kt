package io.glnt.gpms.model.dto.rcs

import io.glnt.gpms.model.dto.entity.CgBasicDTO
import io.glnt.gpms.model.dto.entity.FarePolicyDTO

data class RcsRateInfoDTO(
    val fareBasic: CgBasicDTO? = null,
    val farePolicies: List<FarePolicyDTO>? = null
)
