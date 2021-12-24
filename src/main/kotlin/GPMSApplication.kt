package io.glnt.gpms

/**
 * Create by lucy on 2019-05-21
 **/

import io.github.jhipster.config.JHipsterConstants
import io.glnt.gpms.common.configs.ApplicationProperties
import io.glnt.gpms.common.utils.DefaultProfileUtil
import io.glnt.gpms.model.entity.*
import io.glnt.gpms.model.enums.*
import io.glnt.gpms.model.repository.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.ApplicationPidFileWriter
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.core.env.Environment
import org.springframework.scheduling.annotation.EnableScheduling
import java.net.InetAddress
import java.net.UnknownHostException
import javax.annotation.PostConstruct

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(LiquibaseProperties::class, ApplicationProperties::class)
class GPMSApplication(
    private val env: Environment,
    private val gateRepository: GateRepository,
    private val facilityRepository: ParkFacilityRepository,
    private val displayColorRepository: DisplayColorRepository,
    private val displayInfoRepository: DisplayInfoRepository,
    private val displayMessageRepository: DisplayMessageRepository
) {
    private val log = LoggerFactory.getLogger(GPMSApplication::class.java)

    @PostConstruct
    fun initApplication() {
        val activeProfiles = env.activeProfiles

        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(
                JHipsterConstants.SPRING_PROFILE_PRODUCTION
            )) {
            log.error("You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time.")
        }
        if (activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) && activeProfiles.contains(
                JHipsterConstants.SPRING_PROFILE_CLOUD
            )) {
            log.error("You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time.")
        }

        initDb()
    }

    fun initDb() {
        log.warn("------ Database 초기화 start ------")
        // gate Data 초기화
        gateRepository.findAll().let { gates ->
            if (gates.isEmpty()) {
                log.warn("------ Database 초기화 Gate 정보 ------")
                gateRepository.findByGateId("GATE001")?: run {
                    gateRepository.saveAndFlush(
                        Gate(sn = null, gateId = "GATE001", gateName = "입구게이트1", gateType = GateTypeStatus.IN, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                            seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000001", delYn = YN.N, resetSvr = "http://192.168.20.211/io.cgi?relay=")
                    )
                }
                gateRepository.findByGateId("GATE002")?: run {
                    gateRepository.saveAndFlush(
                        Gate(sn = null, gateId = "GATE002", gateName = "출구게이트1", gateType = GateTypeStatus.OUT, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                            seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000002", delYn = YN.N, resetSvr = "http://192.168.20.211/io.cgi?relay=")
                    )
                }
                gateRepository.findByGateId("GATE003")?: run {
                    gateRepository.saveAndFlush(
                        Gate(sn = null, gateId = "GATE003", gateName = "입구게이트2", gateType = GateTypeStatus.IN, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                            seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000001", delYn = YN.N, resetSvr = "http://192.168.20.212/io.cgi?relay=")
                    )
                }
                gateRepository.findByGateId("GATE004")?: run {
                    gateRepository.saveAndFlush(
                        Gate(sn = null, gateId = "GATE004", gateName = "출구게이트2", gateType = GateTypeStatus.OUT, openAction = OpenActionType.NONE, relaySvr = "http://localhost:9999/v1", relaySvrKey = "GATESVR1",
                            seasonTicketTakeAction = "GATE", takeAction = "GATE", whiteListTakeAction = "GATE", udpGateid = "FCL0000002", delYn = YN.N, resetSvr = "http://192.168.20.212/io.cgi?relay=")
                    )
                }
            }
        }

        // facility Data 초기화
        facilityRepository.findAll().let { facilities ->
            if (facilities.isEmpty()) {
                log.warn("------ Database 초기화 Facility 정보 ------")
                facilityRepository.findByDtFacilitiesId("LPR001101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구1 LPR", dtFacilitiesId = "LPR001101", gateId = "GATE001",
                            ip = "192.168.20.101", port = "0", resetPort = 1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_front", lprType = LprTypeStatus.FRONT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR001201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구1 LPR(후방)", dtFacilitiesId = "LPR001201", gateId = "GATE001",
                            ip = "192.168.20.102", port = "0", resetPort = 1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_back", lprType = LprTypeStatus.BACK, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("DSP001101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "입구1 전광판", dtFacilitiesId = "DSP001101", gateId = "GATE001",
                            ip = "192.168.20.111", port = "5000", resetPort = 1, gateType = GateTypeStatus.IN, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("BRE001101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "입구1 차단기", dtFacilitiesId = "BRE001101", gateId = "GATE001",
                            ip = "192.168.20.121", port = "4001", resetPort = 2, gateType = GateTypeStatus.IN, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR001102")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구1 보조 LPR", dtFacilitiesId = "LPR001102", gateId = "GATE001",
                            ip = "0.0.0.0", port = "0", resetPort = -1, gateType = GateTypeStatus.IN, lprType = LprTypeStatus.ASSIST, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR002101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구1 LPR", dtFacilitiesId = "LPR002101", gateId = "GATE002",
                            ip = "192.168.20.103", port = "0", resetPort = 3, gateType = GateTypeStatus.OUT, imagePath = "C:\\park\\out_front", lprType = LprTypeStatus.FRONT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("DSP002201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "출구1 전광판", dtFacilitiesId = "DSP002201", gateId = "GATE002",
                            ip = "192.168.20.112", port = "5000", resetPort = 3, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("BRE002201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "출구1 차단기", dtFacilitiesId = "BRE002201", gateId = "GATE002",
                            ip = "192.168.20.122", port = "4001", resetPort = 4, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR002102")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구1 보조 LPR", dtFacilitiesId = "LPR002102", gateId = "GATE002",
                            ip = "0.0.0.0", port = "0", resetPort = -1, gateType = GateTypeStatus.OUT, lprType = LprTypeStatus.ASSIST, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("PAY002201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.PAYSTATION, modelid = "MDL0000030", fname = "출구1 정산기", dtFacilitiesId = "PAY002201", gateId = "GATE002",
                            ip = "192.168.20.131", port = "7373", resetPort = 5, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("VOP002201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.VOIP, modelid = "MDL0000032", fname = "출구1 VOIP", dtFacilitiesId = "VOP002201", gateId = "GATE002",
                            ip = "192.168.20.142", port = "0", resetPort = -1, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR003101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구2 LPR", dtFacilitiesId = "LPR003101", gateId = "GATE003",
                            ip = "192.168.20.104", port = "0", resetPort = -1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_front2", lprType = LprTypeStatus.FRONT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR003201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구2 LPR(후방)", dtFacilitiesId = "LPR003201", gateId = "GATE003",
                            ip = "192.168.20.105", port = "0", resetPort = -1, gateType = GateTypeStatus.IN, imagePath = "C:\\park\\in_back2", lprType = LprTypeStatus.BACK, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("DSP003101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "입구2 전광판", dtFacilitiesId = "DSP003101", gateId = "GATE003",
                            ip = "192.168.20.113", port = "5000", resetPort = 0, gateType = GateTypeStatus.IN, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("BRE003101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "입구2 차단기", dtFacilitiesId = "BRE003101", gateId = "GATE003",
                            ip = "192.168.20.123", port = "4001", resetPort = 0, gateType = GateTypeStatus.IN, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR003102")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "입구2 보조 LPR", dtFacilitiesId = "LPR003102", gateId = "GATE003",
                            ip = "0.0.0.0", port = "0", resetPort = 0, gateType = GateTypeStatus.IN, lprType = LprTypeStatus.ASSIST, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR004101")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구2 LPR", dtFacilitiesId = "LPR004101", gateId = "GATE004",
                            ip = "192.168.20.106", port = "0", resetPort = 0, gateType = GateTypeStatus.OUT, imagePath = "C:\\park\\out_front2", lprType = LprTypeStatus.FRONT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("DSP004201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.DISPLAY, modelid = "MDL0000043", fname = "출구2 전광판", dtFacilitiesId = "DSP004201", gateId = "GATE004",
                            ip = "192.168.20.114", port = "5000", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("BRE004201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.BREAKER, modelid = "MDL0000035", fname = "출구2 차단기", dtFacilitiesId = "BRE004201", gateId = "GATE004",
                            ip = "192.168.20.124", port = "4001", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("LPR004102")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.LPR, modelid = "MDL0000029", fname = "출구2 보조 LPR", dtFacilitiesId = "LPR004102", gateId = "GATE004",
                            ip = "0.0.0.0", port = "0", resetPort = 0, gateType = GateTypeStatus.OUT, lprType = LprTypeStatus.ASSIST, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("PAY004201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.PAYSTATION, modelid = "MDL0000030", fname = "출구2 정산기", dtFacilitiesId = "PAY004201", gateId = "GATE004",
                            ip = "192.168.20.132", port = "7373", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
                facilityRepository.findByDtFacilitiesId("VOP004201")?: run {
                    facilityRepository.saveAndFlush(
                        Facility(sn = null, category = FacilityCategoryType.VOIP, modelid = "MDL0000032", fname = "출구2 VOIP", dtFacilitiesId = "VOP004201", gateId = "GATE004",
                            ip = "192.168.20.144", port = "0", resetPort = 0, gateType = GateTypeStatus.OUT, delYn = YN.N)
                    )
                }
            }
        }

        // 전광판 색상 정보
        displayColorRepository.findAll().let { colors ->
            if (colors.isEmpty()) {
                log.warn("------ Database 초기화 Display Color 정보 ------")
                val defaultDisplayColor = ArrayList<DisplayColor>()
                defaultDisplayColor.add(DisplayColor(colorCode = "C1", colorDesc = "초록색", sn = null))
                defaultDisplayColor.add(DisplayColor(colorCode = "C3", colorDesc = "하늘색", sn = null))
                defaultDisplayColor.add(DisplayColor(colorCode = "C4", colorDesc = "빨강색", sn = null))
                defaultDisplayColor.add(DisplayColor(colorCode = "C5", colorDesc = "노랑색", sn = null))
                defaultDisplayColor.forEach { displayColor ->
                    displayColorRepository.findByColorCode(displayColor.colorCode)?:run {
                        displayColorRepository.save(displayColor)
                    }
                }
            }
        }

        // 전광판 action 정보
        displayInfoRepository.findBySn(1)?: run {
            log.warn("------ Database 초기화 Display Info 정보 ------")
            displayInfoRepository.saveAndFlush(DisplayInfo(sn = null, line1Status = DisplayStatus.FIX, line2Status = DisplayStatus.FIX))
        }

        // 입차/출차 reset 메세지 구성
        displayMessageRepository.findAll().let { messages ->
            if (messages.isEmpty()) {
                log.warn("------ Database 초기화 Display Message 정보 ------")
                val defaultDisplayMessages = ArrayList<DisplayMessage>()
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "안녕하세요", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "환영합니다", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "시설물에러", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "-", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "감사합니다", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "안녕히가세요", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "시설물에러", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "-", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.WAIT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "시설물에러", sn = null, delYn = YN.N)
                )
                defaultDisplayMessages.add(
                    DisplayMessage(
                        messageClass = DisplayMessageClass.WAIT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "-", sn = null, delYn = YN.N)
                )

                defaultDisplayMessages.forEach { message ->
                    displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(
                        message.messageClass!!, message.messageType, message.order!!)?:run {
                        displayMessageRepository.saveAndFlush(message)
                    }
                }
            }
        }
        log.warn("------ Database 초기화 end ------")
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
                addListeners(ApplicationPidFileWriter("/tmp/gpms.pid"))
            }.environment

            var protocol = "http"
            if (env.getProperty("server.ssl.key-store") != null) {
                protocol = "https"
            }

            log.warn(
                "\n----------------------------------------------------------\n\t" +
                        "Application '{} Version {}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}\n\t" +
                        "External: \t{}://{}:{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                env.getProperty("spring.application.version"),
                protocol,
                env.getProperty("server.port"),
                protocol,
                InetAddress.getLocalHost().hostAddress,
                env.getProperty("server.port"),
                env.activeProfiles
            )

        }
    }

}
