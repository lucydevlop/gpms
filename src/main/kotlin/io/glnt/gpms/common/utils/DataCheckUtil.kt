package io.glnt.gpms.common.utils

import io.glnt.gpms.common.utils.DateUtil
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern

object DataCheckUtil {
    var strIdx: Int = 65

    fun isValidCarNumber(vehicleNo: String) : Boolean {
        try {
            var regex: String = "([0-9]{2,3})+([가-힣]{1,1})+([0-9]{4})"
            var p: Pattern = Pattern.compile(regex)
            var m: Matcher = p.matcher(vehicleNo)
            if (m.matches()) {
                return true
            } else {
                regex = "^[서울|부산|대구|인천|대전|광주|울산|제주|경기|강원|충남|전남|전북|경남|경북|세종]{2}\\d{2}[가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|허|배|호|하\\x20]\\d{4}$";
                p = Pattern.compile(regex)
                m = p.matcher(vehicleNo)
                if (m.matches()) {
                    return true
                }
            }
            return false
        } catch (e: Exception) {
            return false
        }
    }

    fun generateRequestId(parkId: String?): String {
        if (strIdx > 97) strIdx = 65
        val key = parkId+"_"+DateUtil.stringToNowDateTime()+"_"+strIdx.toChar()
        strIdx++
        return key
    }
}