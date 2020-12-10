package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Corp
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CorpRepository: JpaRepository<Corp, Long> {
    fun findByCorpId(corpId: String) : Corp?
}