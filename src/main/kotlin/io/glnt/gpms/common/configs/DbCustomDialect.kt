package io.glnt.gpms.common.configs

import com.vladmihalcea.hibernate.type.array.IntArrayType
import com.vladmihalcea.hibernate.type.array.StringArrayType
import com.vladmihalcea.hibernate.type.json.JsonBinaryType
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType
import com.vladmihalcea.hibernate.type.json.JsonStringType
import org.hibernate.dialect.MariaDB103Dialect
import java.sql.Types

class DbCustomDialect: MariaDB103Dialect() {
    init {
        registerHibernateType(Types.OTHER, StringArrayType::class.java.name)
        registerHibernateType(Types.OTHER, IntArrayType::class.java.name)
        registerHibernateType(Types.OTHER, JsonStringType::class.java.name)
        registerHibernateType(Types.OTHER, JsonBinaryType::class.java.name)
        registerHibernateType(Types.OTHER, JsonNodeBinaryType::class.java.name)
//        registerHibernateType(Types.OTHER, JsonNodeStringType::class.java.name)
        registerColumnType(Types.OTHER, "clob")
    }
}