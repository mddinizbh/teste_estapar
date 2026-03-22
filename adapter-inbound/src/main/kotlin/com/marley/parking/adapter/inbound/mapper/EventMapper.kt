package com.marley.parking.adapter.inbound.mapper

import com.marley.parking.domain.model.vo.LicensePlate
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeParseException

object EventMapper {

    fun toLicensePlate(value: String?): LicensePlate {
        requireNotNull(value) { "license_plate is required" }
        return LicensePlate(value)
    }

    fun toInstant(value: String?): Instant {
        requireNotNull(value) { "timestamp is required" }
        return try {
            Instant.parse(value)
        } catch (e: DateTimeParseException) {
            LocalDateTime.parse(value).toInstant(ZoneOffset.UTC)
        }
    }
}
