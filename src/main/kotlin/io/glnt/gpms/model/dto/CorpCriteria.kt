package io.glnt.gpms.model.dto

import io.glnt.gpms.model.enums.DelYn
import java.io.Serializable

data class CorpCriteria(
    var sn: Long? = null,

    var corpName: String? = null,

    var corpId: String? = null,

    var tel: String? = null,

    var delYn: DelYn? = null

): Serializable {
    constructor(other: CorpCriteria) :
        this(
            other.sn,
            other.corpName,
            other.corpId,
            other.tel,
            other.delYn
        )

    companion object {
        private const val serialVersionUID: Long = 1L
    }

}
