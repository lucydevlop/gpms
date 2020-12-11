package io.glnt.gmps

import io.glnt.gpms.handler.parkinglot.model.reqAddParkIn
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.unit.junit.Timeout
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Before
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

//    @Before
//    fun setup(context: TestContext) {
//        vertx = Vertx.vertx()
//
//        jwt = JWTAuth.create(vertx, )
//    }
//    private fun givenJwtSetting() {
//        ()
//    }
//    @Test
//    fun get_parksFeatureTest() {
//        withTestApplication()
//    }

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
}