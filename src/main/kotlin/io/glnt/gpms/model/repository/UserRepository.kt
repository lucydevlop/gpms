package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.entity.User
import io.glnt.gpms.model.enums.UserRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<SiteUser, Long> {
    fun findUsersById(id: String) : SiteUser?
    fun findUserByIdx(idx: Long) : SiteUser?
    fun findUsersByRole(role: UserRole): List<SiteUser>?
}

//@Repository
//interface SiteAdminRepository : JpaRepository<SiteAdmin, Long> {
//    fun findByAdminId(id: String) : SiteAdmin?
//}

