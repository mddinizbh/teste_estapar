package com.marley.parking.adapter.inbound.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class ErrorResponse(val error: String, val message: String)
