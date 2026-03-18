package com.marley.parking.domain.model.vo

@JvmInline
value class LicensePlate(val value: String) {
    init {
        require(value.isNotBlank()) { "License plate must not be blank" }
    }
}
