package com.marley.parking.domain.port.inbound

import com.marley.parking.domain.model.vo.LicensePlate
import java.time.Instant

interface VehicleExitUseCase {
    fun execute(licensePlate: LicensePlate, exitTime: Instant)
}
