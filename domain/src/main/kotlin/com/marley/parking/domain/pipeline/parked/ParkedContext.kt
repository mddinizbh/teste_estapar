package com.marley.parking.domain.pipeline.parked

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate
import java.time.Instant

// Fields session and spot are nullable as a conscious trade-off of the pipeline pattern:
// each handler populates its output field, and subsequent handlers consume it via !!.
data class ParkedContext(
    val licensePlate: LicensePlate,
    val coordinates: Coordinates,
    val parkedTime: Instant,
    var session: ParkingSession? = null,
    var spot: Spot? = null
)
