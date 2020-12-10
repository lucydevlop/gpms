package io.glnt.gpms.model.enums

import org.springframework.security.core.GrantedAuthority

enum class UserRole : GrantedAuthority {
    ADMIN,
    USER,
    SHOP,
    CEO,
    LOADER;

    override fun getAuthority(): String = name
}