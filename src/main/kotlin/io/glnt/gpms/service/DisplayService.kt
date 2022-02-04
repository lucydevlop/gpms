package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.DisplayMessageDTO
import io.glnt.gpms.model.dto.response.DisplayResponse
import io.glnt.gpms.model.entity.DisplayInfo
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.mapper.DisplayColorMapper
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
    private val displayInfoRepository: DisplayInfoRepository,
    private val displayColorMapper: DisplayColorMapper
) {
    lateinit var displayMessagesIn: List<DisplayMessageDTO>
    lateinit var displayMessagesOut: List<DisplayMessageDTO>
    lateinit var displayMessagesWait: List<DisplayMessageDTO>

    @PostConstruct
    fun initalizeData() {
        // static data setting
        initStaticData()
    }

    fun initStaticData() {
        val displayMessages = findDisplayMessageAll().filter { displayMessageDTO -> displayMessageDTO.delYn == YN.N }
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

    fun findDisplayMessageBySn(sn: Long): DisplayMessageDTO? {
        return displayMessageRepository.findBySn(sn)?.let { DisplayMessageDTO(it) }
    }

    fun saveDisplayInfo(displayInfo: DisplayInfo): DisplayInfo {
        displayInfoRepository.save(displayInfo)
        return displayInfo
    }

    fun getAll(): DisplayResponse {
        return DisplayResponse(
            messages = displayMessageRepository.findAll().map(displayMessageMapper::toDTO),
            info = displayInfoRepository.findAll().first(),
            colors = displayColorRepository.findAll().map(displayColorMapper::toDTO)
        )
    }


}