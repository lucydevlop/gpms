package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.DisplayMessageDTO
import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayInfo
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.DisplayStatus
import io.glnt.gpms.model.mapper.DisplayMessageMapper
import io.glnt.gpms.model.repository.DisplayColorRepository
import io.glnt.gpms.model.repository.DisplayInfoRepository
import io.glnt.gpms.model.repository.DisplayMessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import javax.annotation.PostConstruct

@Service
class DisplayService(
    private val displayMessageMapper: DisplayMessageMapper,
    private val displayMessageRepository: DisplayMessageRepository,
    private val displayColorRepository: DisplayColorRepository,
    private val displayInfoRepository: DisplayInfoRepository
) {
    lateinit var displayMessagesIn: List<DisplayMessageDTO>
    lateinit var displayMessagesOut: List<DisplayMessageDTO>
    lateinit var displayMessagesWait: List<DisplayMessageDTO>

    @PostConstruct
    fun initalizeData() {
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

        displayInfoRepository.findBySn(1)?: run {
            displayInfoRepository.saveAndFlush(DisplayInfo(sn = null, line1Status = DisplayStatus.FIX, line2Status = DisplayStatus.FIX))
        }

        // 입차/출차 reset 메세지 구성
        val defaultDisplayMessages = ArrayList<DisplayMessageDTO>()
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "안녕하세요", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "환영합니다", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "시설물에러", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.IN, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "-", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "감사합니다", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.INIT, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "안녕히가세요", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "시설물에러", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.OUT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "-", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.WAIT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 1, lineNumber = 1, colorCode = "C1", messageDesc = "시설물에러", sn = null, delYn = DelYn.N)
        )
        defaultDisplayMessages.add(
            DisplayMessageDTO(
                messageClass = DisplayMessageClass.WAIT, messageType = DisplayMessageType.ERROR, messageCode = "ALL", order = 2, lineNumber = 2, colorCode = "C3", messageDesc = "-", sn = null, delYn = DelYn.N)
        )

        defaultDisplayMessages.forEach { message ->
            displayMessageRepository.findByMessageClassAndMessageTypeAndOrder(message.messageClass!!, message.messageType!!, message.order!!)?:run {
                saveDisplayMessage(message)
            }
        }
        // static data setting
        initStaticData()
    }

    fun initStaticData() {
        val displayMessages = findDisplayMessageAll().filter { displayMessageDTO -> displayMessageDTO.delYn == DelYn.N }
        displayMessagesIn = displayMessages.filter { displayMessageDTO -> displayMessageDTO.messageClass == DisplayMessageClass.IN }
        displayMessagesOut = displayMessages.filter { displayMessageDTO -> displayMessageDTO.messageClass == DisplayMessageClass.OUT }
        displayMessagesWait = displayMessages.filter { displayMessageDTO -> displayMessageDTO.messageClass == DisplayMessageClass.WAIT }
    }

    @Transactional(readOnly = true)
    fun findDisplayMessageAll(): List<DisplayMessageDTO> {
        return displayMessageRepository.findAll().map(displayMessageMapper::toDTO)
    }

    fun saveDisplayMessage(displayMessageDTO: DisplayMessageDTO): DisplayMessageDTO {
        var displayMessage = displayMessageMapper.toEntity(displayMessageDTO)
        displayMessage = displayMessageRepository.save(displayMessage!!)
        initStaticData()
        return displayMessageMapper.toDTO(displayMessage)
    }
}