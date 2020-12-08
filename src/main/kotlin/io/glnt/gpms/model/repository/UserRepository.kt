package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findUsersByAdminId(id: String) : User?

}