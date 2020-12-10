package io.glnt.gpms.security

import com.fasterxml.jackson.annotation.JsonIgnore
import io.glnt.gpms.model.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class UserPrincipal(
    val idx: Long,

    @JsonIgnore
    val id: String,

    val name: String,

    val phone: String,

    @JsonIgnore
    private val password: String,

    private val authorities: MutableCollection<out GrantedAuthority>
) : UserDetails {

    constructor(user: User): this(
        idx = user.idx,
        id = user.adminId,
        name = user.adminName,
        phone = user.adminPhone,
        password = user.adminPw,
        authorities = Collections.singletonList(SimpleGrantedAuthority(user.role.name))
    )

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

    override fun isEnabled(): Boolean = true

    override fun getUsername(): String = username

    override fun getPassword(): String = password

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as UserPrincipal?
        return id == that?.id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }
}