package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DiscountApplyCriteriaType

data class DiscountApplyDTO (
    var criteria: DiscountApplyCriteriaType? = DiscountApplyCriteriaType.FRONT,

    var baseFeeInclude: DelYn? = DelYn.Y
)
