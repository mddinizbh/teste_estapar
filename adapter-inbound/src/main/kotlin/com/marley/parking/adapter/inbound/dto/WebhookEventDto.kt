package com.marley.parking.adapter.inbound.dto

import io.micronaut.serde.annotation.Serdeable
import jakarta.validation.constraints.NotBlank

@Serdeable
data class WebhookEventDto(
    @field:NotBlank
    val event_type: String,
    val license_plate: String? = null,
    val entry_time: String? = null,
    val exit_time: String? = null,
    val lat: Double? = null,
    val lng: Double? = null
)
