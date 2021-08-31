package io.glnt.gpms.model.repository

import io.glnt.gpms.model.entity.Barcode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface BarcodeRepository : JpaRepository<Barcode, Long> {
}