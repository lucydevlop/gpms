package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.CorpDTO
import io.glnt.gpms.model.dto.entity.CorpTicketDTO
import io.glnt.gpms.model.dto.entity.CorpTicketHistoryDTO
import io.glnt.gpms.model.criteria.CorpCriteria
import io.glnt.gpms.model.dto.*
import io.glnt.gpms.model.entity.CorpTicketInfo
import io.glnt.gpms.model.enums.YN
import io.glnt.gpms.model.mapper.CorpMapper
import io.glnt.gpms.model.mapper.CorpTicketHistoryMapper
import io.glnt.gpms.model.mapper.CorpTicketMapper
import io.glnt.gpms.model.repository.CorpRepository
import io.glnt.gpms.model.repository.CorpTicketHistoryRepository
import io.glnt.gpms.model.repository.CorpTicketRepository
import io.glnt.gpms.model.repository.UserRepository
import mu.KLogging
import okhttp3.internal.format
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.*

@Service
class CorpService(
    private val userRepository: UserRepository,
    private val corpTicketRepository: CorpTicketRepository,
    private val corpRepository: CorpRepository,
    private val corpTicketMapper: CorpTicketMapper,
    private val corpMapper: CorpMapper,
    private val corpQueryService: CorpQueryService,
    private val corpTicketHistoryRepository: CorpTicketHistoryRepository,
    private val corpTicketHistoryMapper: CorpTicketHistoryMapper,
    private val passwordEncoder: PasswordEncoder
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

    fun getStoreByCorpName(name: String): CorpDTO? {
        val criteria = CorpCriteria(corpName = name)
        return corpQueryService.findByCriteria(criteria).firstOrNull()
    }

    fun getAllCorpTickets(): MutableList<CorpTicketDTO> {
        return corpTicketRepository.findAll().mapTo(mutableListOf(), corpTicketMapper::toDTO)
    }

    fun getCorpTicketsByCorpSn(sn: Long) : MutableList<CorpTicketDTO> {
        return corpTicketRepository.findByCorpSn(sn).mapTo(mutableListOf(), corpTicketMapper::toDTO)
    }

    fun getStoreTicketsByStoreSnAndInSn(sn: Long, inSn: Long) {
        getCorpTicketsByCorpSn(sn)
    }

    fun save(corpDTO: CorpDTO, action: String, siteId: String?): CorpDTO {
        logger.debug("Request to save Corp : $corpDTO")
        val corp = corpMapper.toEntity(corpDTO)
        corpRepository.save(corp!!)
        if (action == "create") {
            corp.corpId = siteId+"_"+ format("%05d", corp.sn!!)
            corpRepository.save(corp)
        }

        // 사용자 정보도 update 해줌
        userRepository.findUsersById(corpDTO.corpId?: "")?.let { siteUser ->
            siteUser.delYn = corpDTO.delYn

            if (corpDTO.password != null && corpDTO.password != "" && corpDTO.password != " ") {
                siteUser.password = passwordEncoder.encode(corpDTO.password!!)
            }
            userRepository.save(siteUser)
        }

        return corpMapper.toDTO(corp)
    }

    fun getCorpTicketHistByTicketSn(ticketSn: Long): MutableList<CorpTicketHistoryDTO>? {
        return corpTicketHistoryRepository.findByTicketSnAndDelYn(ticketSn, YN.N)
            ?.mapTo(mutableListOf(), corpTicketHistoryMapper::toDTO)
    }

    fun addCorpTickets(addCorpTicketDTO: AddCorpTicketDTO) {
        var corpTicketInfo = CorpTicketInfo(sn = null, corpSn = addCorpTicketDTO.corpSn, classSn = addCorpTicketDTO.corpTicketClassSn,
            totalQuantity = addCorpTicketDTO.cnt, useQuantity = 0, delYn = YN.N)

        corpTicketRepository.findByCorpSnAndClassSnAndDelYn(addCorpTicketDTO.corpSn, addCorpTicketDTO.corpTicketClassSn, YN.N)?.let { it ->
            corpTicketInfo = it
            corpTicketInfo.totalQuantity = corpTicketInfo.totalQuantity.plus(addCorpTicketDTO.cnt)

        }

        saveCorpTicket(corpTicketMapper.toDTO(corpTicketInfo)).apply {
            saveCorpTicketHistory(
                CorpTicketHistoryDTO(sn = null, ticketSn = this.sn!!, totalQuantity = addCorpTicketDTO.cnt,
                    effectDate = LocalDateTime.now(), delYn = YN.N)
            )
        }
    }

    fun saveCorpTicket(corpTicketDTO: CorpTicketDTO) : CorpTicketDTO {
        logger.debug("Request to save Corp Ticket Info: $corpTicketDTO")
        val corpTicket = corpTicketMapper.toEntity(corpTicketDTO)
        corpTicketRepository.save(corpTicket!!)
        return corpTicketMapper.toDTO(corpTicket)
    }

    fun saveCorpTicketHistory(corpTicketHistoryDTO: CorpTicketHistoryDTO): CorpTicketHistoryDTO {
        logger.debug("Request to save Corp Ticket History: $corpTicketHistoryDTO")
        val corpTicketHistory = corpTicketHistoryMapper.toEntity(corpTicketHistoryDTO)
        corpTicketHistoryRepository.save(corpTicketHistory!!)
        return corpTicketHistoryMapper.toDTO(corpTicketHistory)
    }
}