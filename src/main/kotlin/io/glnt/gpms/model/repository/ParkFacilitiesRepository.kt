package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.DisplayColor
import io.glnt.gpms.model.enums.DisplayPosition
import io.glnt.gpms.model.enums.DisplayType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DisplayColorRepository: JpaRepository<DisplayColor, Long> {
    fun findByPositionAndType(position: DisplayPosition, type: DisplayType): DisplayColor?
    fun findByPositionIn(positions: List<DisplayPosition>): List<DisplayColor>
}

