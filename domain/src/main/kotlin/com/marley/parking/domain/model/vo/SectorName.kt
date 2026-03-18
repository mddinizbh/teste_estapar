package com.marley.parking.domain.model.vo

@JvmInline
value class SectorName(val value: String) {
    init {
        require(value.isNotBlank()) { "Sector name must not be blank" }
    }
}
