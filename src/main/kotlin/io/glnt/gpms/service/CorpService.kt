package io.glnt.gpms.service

import io.github.jhipster.service.filter.LongFilter
import io.github.jhipster.service.filter.StringFilter
import io.glnt.gpms.handler.parkinglot.service.ParkinglotService
import io.glnt.gpms.model.dto.CorpCriteria
import io.glnt.gpms.model.dto.CorpDTO
import io.glnt.gpms.model.dto.CorpTicketDTO
import io.glnt.gpms.model.mapper.CorpMapper
import io.glnt.gpms.model.mapper.CorpTicketMapper
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.CorpTicketRepository
import mu.KLogging
import okhttp3.internal.format
import org.springframework.stereotype.Service
import java.util.*

@Service
class CorpService(
    private val corpTicketRepository: CorpTicketRepository,
    private val corpRepository: CorpRepository,
    private val corpTicketMapper: CorpTicketMapper,
    private val corpMapper: CorpMapper,
    private val corpQueryService: CorpQueryService
) {
    companion object : KLogging()

//    @Transactional(readOnly = true)
//    fun findAll(): List<CorpDTO> {
//        return corpRepository.findAll().map(corpMapper::toDto)
//    }

    fun getStoreBySn(sn: Long): Optional<CorpDTO> {
        val criteria = CorpCriteria(sn = sn)
        return corpQueryService.findByCriteria(criteria).stream().findFirst()
    }

    fun getStoreById(id: String): Optional<CorpDTO> {
        val criteria = CorpCriteria(corpId = id)
        return corpQueryService.findByCriteria(criteria).stream().findFirst()
    }

    fun getStoreByCorpName(name: String): Optional<CorpDTO> {
        val criteria = CorpCriteria(corpId = name)
        return corpQueryService.findByCriteria(criteria).stream().findFirst()
    }

    fun getStoreTicketsByStoreSn(sn: Long) : MutableList<CorpTicketDTO> {
        return corpTicketRepository.findByCorpSn(sn).mapTo(mutableListOf(), corpTicketMapper::toDTO)
    }

    fun getStoreTicketsByStoreSnAndInSn(sn: Long, inSn: Long) {
        getStoreTicketsByStoreSn(sn)
    }

    fun save(corpDTO: CorpDTO, action: String, siteId: String?): CorpDTO {
        logger.debug("Request to save Corp : $corpDTO")
        val corp = corpMapper.toEntity(corpDTO)
        corpRepository.save(corp!!)
        if (action == "create") {
            corp.corpId = siteId+"_"+ format("%05d", corp.sn!!)
            corpRepository.save(corp)
        }
        return corpMapper.toDTO(corp)
    }
}