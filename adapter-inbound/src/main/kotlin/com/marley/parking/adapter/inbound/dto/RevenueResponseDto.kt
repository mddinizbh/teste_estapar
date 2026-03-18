package com.marley.parking.adapter.inbound.dto

import io.micronaut.serde.annotation.Serdeable
import java.math.BigDecimal

@Serdeable
data class RevenueResponseDto(
    val amount: BigDecimal,
    val currency: String = "BRL",
    val timestamp: String
)
