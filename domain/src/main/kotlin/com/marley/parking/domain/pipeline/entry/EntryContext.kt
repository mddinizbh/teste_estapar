package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.OccupancyRate
import java.time.Instant

data class EntryContext(
    val licensePlate: LicensePlate,
    val entryTime: Instant,
    var sector: Sector? = null,
    var occupancyRate: OccupancyRate? = null,
    var priceAtEntry: Money? = null,
    var reservedSpot: Spot? = null,
    var session: ParkingSession? = null
)
