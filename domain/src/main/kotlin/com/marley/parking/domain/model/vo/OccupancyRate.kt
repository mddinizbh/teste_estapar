package com.marley.parking.domain.model.vo

@JvmInline
value class OccupancyRate(val value: Double) {
    init {
        require(value in 0.0..1.0) { "Occupancy rate must be between 0.0 and 1.0" }
    }
}
