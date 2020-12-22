package io.glnt.gpms.security

import com.fasterxml.jackson.annotation.JsonIgnore
import io.glnt.gpms.model.entity.SiteUser
import io.glnt.gpms.model.entity.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.util.*

class UserPrincipal(
    val idx: Long,

    @JsonIgnore
    val id: String,

    val userName: String,

    val phone: String,

    @JsonIgnore
    private val password: String,

    private val authorities: MutableCollection<out GrantedAuthority>
) : UserDetails {

    constructor(user: SiteUser): this(
        idx = user.idx!!,
        id = user.id,
        userName = user.userName,
        phone = user.userPhone!!,
        password = user.password,
        authorities = Collections.singletonList(SimpleGrantedAuthority(user.role!!.name))
    )

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> = authorities

    override fun isEnabled(): Boolean = true

    override fun getUsername(): String = userName

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