package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Failure
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface FailureRepository : JpaRepository<Failure, Long>, JpaSpecificationExecutor<Failure> {
    fun findTopByFacilitiesIdAndFailureCodeAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(facilityId: String, failureCode: String): Failure?
    fun findTopByFacilitiesIdAndExpireDateTimeIsNullOrderByIssueDateTimeDesc(facilityId: String): Failure?
}