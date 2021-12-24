package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.DiscountApplyCriteriaType

data class DiscountApplyDTO (
    var criteria: DiscountApplyCriteriaType? = DiscountApplyCriteriaType.FRONT,

    var baseFeeInclude: YN? = YN.Y
)
