package com.marley.parking.domain.port.inbound

import com.marley.parking.domain.model.vo.LicensePlate
import java.time.Instant

interface VehicleEntryUseCase {
    fun execute(licensePlate: LicensePlate, entryTime: Instant)
}
