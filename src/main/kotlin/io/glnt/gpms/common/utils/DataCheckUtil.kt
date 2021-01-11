package io.glnt.gpms.common.utils

import io.glnt.gpms.common.configs.EnvironmentConfig
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import okhttp3.internal.format
import org.springframework.beans.factory.annotation.Autowired
import java.lang.Exception
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.random.Random

object DataCheckUtil {

    var strIdx: Int = 65

    fun isValidCarNumber(vehicleNo: String) : Boolean {
        try {
//            var regex =
            var p: Pattern = Pattern.compile("([0-9]{2,3})([가-힣]{1})([0-9]{4})")
            var m: Matcher = p.matcher(vehicleNo)
            if (m.matches()) {
                return true
            } else {
                val regex = "^[서울|부산|대구|인천|대전|광주|울산|제주|경기|강원|충남|전남|전북|경남|경북|세종]{2}\\d{2}[가|나|다|라|마|거|너|더|러|머|버|서|어|저|고|노|도|로|모|보|소|오|조|구|누|두|루|무|부|수|우|주|바|사|아|자|허|배|호|하\\x20]\\d{4}$";
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

    fun generateRequestId(parkSiteId: String): String {
        if (strIdx > 97) strIdx = 65
        val key = parkSiteId+"_"+DateUtil.stringToNowDateTime()+"_"+strIdx.toChar()
        strIdx++
        return key
    }

    fun generateSessionId(pre: String): String {
        val r = Random
        return pre+DateUtil.stringToNowDateTime()+
                format("%03d", r.nextInt(1, 999))
    }

    fun getFileName(fileFullPath: String) : String{
        return fileFullPath.substring(fileFullPath.lastIndexOf("/")+1)
    }
}