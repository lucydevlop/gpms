package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.entity.User
import io.glnt.gpms.model.enums.DelYn
import io.glnt.gpms.model.enums.UserRole
import org.springframework.data.jpa.domain.Specification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<SiteUser, Long> {
    fun findUsersById(id: String) : SiteUser?
    fun findUsersByIdAndDelYn(id: String, delYn: DelYn) : SiteUser?
    fun findUserByIdx(idx: Long) : SiteUser?
    fun findByIdx(idx: Long): Optional<SiteUser>
    fun findUsersByRole(role: UserRole): List<SiteUser>?
    fun findAll(specification: Specification<SiteUser>): List<SiteUser>?
}

//@Repository
//interface SiteAdminRepository : JpaRepository<SiteAdmin, Long> {
//    fun findByAdminId(id: String) : SiteAdmin?
//}

