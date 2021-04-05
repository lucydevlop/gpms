package io.glnt.gpms.model.enums

import org.springframework.security.core.GrantedAuthority

enum class UserRole : GrantedAuthority {
    SUPER_ADMIN,
    ADMIN,
    OPERATION,
    USER,
    OPERATION,
    STORE;
    override fun getAuthority(): String = name
}