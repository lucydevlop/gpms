package io.glnt.gpms.service

import io.github.jhipster.service.QueryService
import io.glnt.gpms.model.entity.ParkIn
import io.glnt.gpms.model.repository.ParkInRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ParkInQueryService(
    private val parkInRepository: ParkInRepository
): QueryService<ParkIn>() {
    companion object : KLogging()

}