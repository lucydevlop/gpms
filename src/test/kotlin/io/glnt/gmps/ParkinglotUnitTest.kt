package io.glnt.gmps

import io.vertx.core.Vertx
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.unit.junit.Timeout
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(VertxUnitRunner::class)
class ParkinglotUnitTest {

    @get:Rule
    val timeout = Timeout.seconds(5)

    lateinit var vertx: Vertx
    lateinit var jwt: JWTAuth

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
}