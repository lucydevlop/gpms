package io.glnt.gpms.handler.facility.model

import io.glnt.gpms.model.entity.Facility

data class reqUpdateFacilities(
    var facilities: ArrayList<Facility>
)