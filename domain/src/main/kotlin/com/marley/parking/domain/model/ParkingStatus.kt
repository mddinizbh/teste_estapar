package com.marley.parking.domain.model

enum class ParkingStatus {
    ENTERED,
    PARKED,
    EXITED;

    companion object {
        val ACTIVE_STATUSES = listOf(ENTERED, PARKED)
    }
}
