package com.marley.parking.domain.model.vo

@JvmInline
value class LicensePlate(val value: String) {
    init {
        require(value.isNotBlank()) { "License plate must not be blank" }
        require(FORMAT.matches(value)) { "Invalid license plate format: $value" }
    }

    companion object {
        private val FORMAT = Regex("^[A-Z]{2,3}-?[A-Z0-9]\\d[A-Z0-9]\\d{1,4}$")
    }
}
