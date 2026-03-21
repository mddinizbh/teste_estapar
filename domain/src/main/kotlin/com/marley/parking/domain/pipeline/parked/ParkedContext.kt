package com.marley.parking.domain.pipeline.parked

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate
import java.time.Instant

data class ParkedContext(
    val licensePlate: LicensePlate,
    val coordinates: Coordinates,
    val parkedTime: Instant,
    val session: ParkingSession? = null,
    val spot: Spot? = null
)
