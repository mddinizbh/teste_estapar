package com.marley.parking.domain.service

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.OccupancyRate
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil

class PricingService {

    fun calculateEntryPrice(basePrice: Money, occupancyRate: OccupancyRate): Money {
        val modifier = resolveModifier(occupancyRate)
        val adjusted = basePrice.value.multiply(BigDecimal.ONE.add(modifier))
        return Money(adjusted.setScale(2, RoundingMode.HALF_UP))
    }

    fun calculateCharge(priceAtEntry: Money, entryTime: Instant, exitTime: Instant): Money {
        val minutes = Duration.between(entryTime, exitTime).toMinutes()
        if (minutes <= 30) return Money(BigDecimal.ZERO)

        val hours = ceil(minutes.toDouble() / 60.0).toLong()
        return priceAtEntry * hours
    }

    private fun resolveModifier(occupancyRate: OccupancyRate): BigDecimal {
        val rate = occupancyRate.value
        return when {
            rate < 0.25 -> BigDecimal("-0.10")
            rate < 0.50 -> BigDecimal.ZERO
            rate < 0.75 -> BigDecimal("0.10")
            else -> BigDecimal("0.25")
        }
    }
}
