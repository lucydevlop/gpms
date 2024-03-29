package io.glnt.gmps

import io.glnt.gpms.common.utils.DataCheckUtil
import io.glnt.gpms.common.utils.DateUtil
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.enums.VehicleType
import io.glnt.gpms.service.CalcService
import io.glnt.gpms.service.RelayService
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.unit.junit.Timeout
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import java.io.File
import java.util.*

@RunWith(VertxUnitRunner::class)
class ParkinglotUnitTest {

    @get:Rule
    val timeout = Timeout.seconds(5)

    lateinit var vertx: Vertx
    lateinit var jwt: JWTAuth

    @Autowired
    private lateinit var parkinglotService: ParkinglotService

    @Autowired
    private lateinit var relayService: RelayService

    @Autowired
    private lateinit var calcService: CalcService

    @Test
    fun test_parkIn() {
//        val test = reqAddParkIn(vehicleNo = "11가1234")
//        parkinglotService.parkIn(request = test)
    }

    @Test
    fun test_fileConvert() {
        val filePath = "/Users/lucy/project/glnt/parking/gpms/67다8183.jpg"
        val bytes = File(filePath).readBytes()
        val base64 = Base64.getEncoder().encodeToString(bytes)

        println("Base64ImageString = $base64")
    }

    @Test
    fun test_carNoisValidation() {
        DataCheckUtil.isValidCarNumber("서울67다8183")
//        val p: Pattern = Pattern.compile("([0-9]{2,3})([가-힣]{1})([0-9]{4})")
////        val m: Matcher = p.matcher("17기1111")
////        val m: Matcher = p.matcher("67다8183")
//        val m: Matcher = p.matcher("67라8183")
//        assertTrue(m.matches())
    }

    @Test
    fun test_yesterday() {
        val today = DateUtil.formatDateTime(DateUtil.stringToLocalDateTime("2018-10-01 00:00:00", "yyyy-MM-dd HH:mm:ss"), "yyyyMMddHHmmss").substring(0, 8)+"235959"
        val yesterday = DateUtil.getAddDays(DateUtil.stringToLocalDateTime(today), -1)
        println("Base64ImageString = $today $yesterday")
    }

    @Test
    fun paymentHealthCheck() {
        relayService.paymentHealthCheck()
    }

    @Test
    fun failure() {
//        var relayService =  RelayService()
//        relayService.saveFailure(
//            Failure( sn = null,
//                     issueDateTime = LocalDateTime.now(),
//                     facilitiesId = "BRE001101",
//                     fName = "입구1 차단기",
//                     failureCode = "crossingGateLongTimeOpen",
//                     failureType = "ERROR"
//        ))
    }

    @Test
    fun calcParkinglotFee() {
        calcService.calcParkinglotFee(
            inTime = DateUtil.stringToLocalDateTime("2021-12-24 00:00:00", "yyyy-MM-dd HH:mm:ss"),
            outTime = DateUtil.stringToLocalDateTime("2021-12-25 02:00:00", "yyyy-MM-dd HH:mm:ss"),
            vehicleType = VehicleType.SMALL,
            vehicleNo = "11호1111",
            //type = null,
            discountMin = 0, inSn = 0, discountClasses = null, isReCharge = false)
    }


}