package com.marley.parking.adapter.inbound.dto

import io.micronaut.serde.annotation.Serdeable

@Serdeable
data class RevenueRequestDto(
    val date: String,
    val sector: String
)
