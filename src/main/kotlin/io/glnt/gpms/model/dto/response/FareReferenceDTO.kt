package io.glnt.gpms.model.dto.response

import io.glnt.gpms.model.dto.entity.CgBasicDTO
import io.glnt.gpms.model.dto.entity.FareInfoDTO
import io.glnt.gpms.model.dto.entity.FarePolicyDTO

data class FareReferenceDTO(
    val fareInfos: List<FareInfoDTO>,
    val farePolicies: List<FarePolicyDTO>,
    val fareBasic: CgBasicDTO?
)
