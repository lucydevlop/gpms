package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.entity.DisplayMessage
import io.glnt.gpms.model.enums.DisplayMessageClass
import io.glnt.gpms.model.enums.DisplayMessageType
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DisplayColorRepository: JpaRepository<DisplayColor, Long> {
    fun findByPositionAndType(position: DisplayPosition, type: DisplayType): DisplayColor?
    fun findByPositionIn(positions: List<DisplayPosition>): List<DisplayColor>
}

@Repository
interface DisplayMessageRepository: JpaRepository<DisplayMessage, Long> {
    fun findByMessageClassAndMessageTypeAndOrder(messageClass: DisplayMessageClass, messageType: DisplayMessageType, order: Int) : DisplayMessage?
    fun findByMessageClass(messageClass: DisplayMessageClass) : List<DisplayMessage>?
}
