package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayInfo
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
//import io.glnt.gpms.model.enums.DisplayType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DisplayColorRepository: JpaRepository<DisplayColor, Long> {
    fun findByColorCode(colorCode: String): DisplayColor?
//    fun findByMessageClassIn(messageClasses: List<DisplayMessageClass>): List<DisplayColor>
}

@Repository
interface DisplayMessageRepository: JpaRepository<DisplayMessage, Long> {
    fun findByMessageClassAndMessageTypeAndOrder(messageClass: DisplayMessageClass, messageType: DisplayMessageType, order: Int) : DisplayMessage?
    fun findByMessageClass(messageClass: DisplayMessageClass) : List<DisplayMessage>?
    fun findByMessageClassAndDelYn(messageClass: DisplayMessageClass, delYn: YN): List<DisplayMessage>?
    fun findBySn(sn: Long): DisplayMessage?
}

@Repository
interface DisplayInfoRepository: JpaRepository<DisplayInfo, Long> {
    fun findBySn(sn: Long): DisplayInfo?
}
