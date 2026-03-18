package com.marley.parking.domain.model.vo

import java.math.BigDecimal

@JvmInline
value class Money(val value: BigDecimal) {
    init {
        require(value >= BigDecimal.ZERO) { "Money must be non-negative" }
    }

    operator fun times(hours: Long): Money = Money(value.multiply(BigDecimal(hours)))
}
