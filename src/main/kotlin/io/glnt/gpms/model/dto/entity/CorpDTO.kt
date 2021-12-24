package io.glnt.gpms.model.dto.entity

import io.glnt.gpms.model.entity.Corp
import io.glnt.gpms.model.enums.YN
import java.io.Serializable
import java.time.LocalDateTime
import javax.persistence.EnumType
import javax.persistence.Enumerated
import javax.validation.constraints.NotNull

data class CorpDTO (
    var sn: Long? = null,

    var corpId: String? = null,

    var corpName: String? = null,

    var form: Int? = null,

    var resident: Int? = null,

    var dong: String? = null,

    var ho: String? = null,

    var ceoName: String? = null,

    var tel: String? = null,

    var mobile: String? = null,

    var email: String? = null,

    var address: String? = null,

    var saupNo: String? = null,

    var mobileNo: String? = null,

    var balance: Int? = null,

    var lastCharging: Long? = null,

    var lastDiscount: Long? = null,

    var balanceUpdate: LocalDateTime? = null,

    var updateDate: LocalDateTime? = null,

    var password: String? = null,

    @get: NotNull
    @Enumerated(EnumType.STRING)
    var delYn: YN? = null
): Serializable {

    constructor(corp: Corp) :
        this(
            corp.sn, corp.corpId, corp.corpName, corp.form, corp.resident, corp.dong,
            corp.ho, corp.ceoName, corp.tel, corp.mobile, corp.email, corp.address, corp.saupNo,
            corp.mobileNo, corp.balance, corp.lastCharging, corp.lastDiscount, corp.balanceUpdate,
            corp.UpdateDate, null, corp.delYn
        )
}