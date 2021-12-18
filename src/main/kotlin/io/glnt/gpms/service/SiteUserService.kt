package io.glnt.gpms.service

import io.glnt.gpms.model.dto.entity.SiteUserDTO
import io.glnt.gpms.model.mapper.SiteUserMapper
import io.glnt.gpms.model.repository.UserRepository
import mu.KLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class SiteUserService (
    private val siteUserRepository: UserRepository,
    private val siteUserMapper: SiteUserMapper
){
    companion object : KLogging()

    @Transactional(readOnly = true)
    fun findAll(): List<SiteUserDTO> {
        return siteUserRepository.findAll().map(siteUserMapper::toDTO)
    }

    fun findByIdx(idx: Long): Optional<SiteUserDTO> {
        logger.debug { "Request to get SiteUser By Idx $idx" }
        return siteUserRepository.findByIdx(idx).map(siteUserMapper::toDTO)
    }

    fun save(siteUserDTO: SiteUserDTO): SiteUserDTO {
        var siteUser = siteUserMapper.toEntity(siteUserDTO)
        siteUser = siteUserRepository.save(siteUser!!)
        return siteUserMapper.toDTO(siteUser)
    }

}