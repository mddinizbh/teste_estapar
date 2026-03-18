package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import java.time.Instant

data class ExitContext(
    val licensePlate: LicensePlate,
    val exitTime: Instant,
    var session: ParkingSession? = null,
    var amountCharged: Money? = null,
    var spot: Spot? = null
)
