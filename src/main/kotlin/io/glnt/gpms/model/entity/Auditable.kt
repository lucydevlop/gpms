package io.glnt.gpms.model.entity

import org.springframework.data.annotation.CreatedBy
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedBy
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import org.springframework.format.annotation.DateTimeFormat
import java.time.LocalDateTime
import javax.persistence.Column
import javax.persistence.EntityListeners
import javax.persistence.MappedSuperclass

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class Auditable {
    @CreatedBy
    @Column(name = "create_by" ,nullable = false, updatable = false)
    var createdBy: String? = null

    @CreatedDate
    @Column(name = "create_date" ,nullable = false, updatable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var createDate: LocalDateTime? = null

    @LastModifiedBy
    @Column(name = "update_by", nullable = false)
    var updateBy: String? = null

    @LastModifiedDate
    @Column(name = "update_date", nullable = false)
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    var UpdateDate: LocalDateTime? = null
}