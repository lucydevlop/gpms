package io.glnt.gpms

/**
 * Create by lucy on 2019-05-21
 **/
import io.github.jhipster.config.JHipsterConstants
import io.glnt.gpms.common.configs.ApplicationProperties
import io.glnt.gpms.common.utils.DefaultProfileUtil
import io.glnt.gpms.handler.calc.service.FeeCalculation
import io.glnt.gpms.handler.facility.service.FacilityService
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.handler.relay.service.RelayService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import java.net.InetAddress
import java.net.UnknownHostException
import javax.annotation.PostConstruct

@SpringBootApplication
@EnableConfigurationProperties(LiquibaseProperties::class, ApplicationProperties::class)
class GPMSApplication(
    private val env: Environment,
    val parkinglotService: ParkinglotService,
    val facilityService: FacilityService,
    val relayService: RelayService,
    val feeCalculation: FeeCalculation
) {
    private val log = LoggerFactory.getLogger(GPMSApplication::class.java)

    @PostConstruct
    fun initApplication() {
        val activeProfiles = env.activeProfiles
        
        parkinglotService.fetchParkSiteInfo()
        facilityService.fetchDisplayColor()
        facilityService.fetchGate()
        relayService.fetchParkAlarmSetting(parkinglotService.parkSite.siteid)
        feeCalculation.init()

        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)) {
            log.error("You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time.")
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)) {
            log.error("You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time.")
        }
    }

    companion object {
        /**
         * Main method, used to run the application.
         *
         * @param args the command line arguments
         * @throws UnknownHostException if the local host name could not be resolved into an cim
         */
        @Throws(UnknownHostException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val log = LoggerFactory.getLogger(GPMSApplication::class.java)

            val env = runApplication<GPMSApplication> {
                setDefaultProperties(DefaultProfileUtil.addDefaultProfile())
            }.environment

            var protocol = "http"
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https"
            }
            log.info("\n----------------------------------------------------------\n\t" +
                    "Application '{}' is running! Access URLs:\n\t" +
                    "Local: \t\t{}://localhost:{}\n\t" +
                    "External: \t{}://{}:{}\n\t" +
                    "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().hostAddress,
                env.getProperty("server.port"),
                env.activeProfiles)

        }
    }

}
