package io.glnt.gpms.model.criteria

import io.glnt.gpms.model.enums.YN
import java.io.Serializable

data class CorpCriteria(
    var sn: Long? = null,

    var corpName: String? = null,

    var corpId: String? = null,

    var tel: String? = null,

    var delYn: YN? = null

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
