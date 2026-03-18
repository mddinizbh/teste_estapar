package com.marley.parking.adapter.inbound.mapper

import com.marley.parking.domain.model.vo.LicensePlate
import java.time.Instant

object EventMapper {

    fun toLicensePlate(value: String?): LicensePlate {
        requireNotNull(value) { "license_plate is required" }
        return LicensePlate(value)
    }

    fun toInstant(value: String?): Instant {
        requireNotNull(value) { "timestamp is required" }
        return Instant.parse(value)
    }
}
