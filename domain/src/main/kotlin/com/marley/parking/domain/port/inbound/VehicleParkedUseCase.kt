package com.marley.parking.domain.port.inbound

import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate

interface VehicleParkedUseCase {
    fun execute(licensePlate: LicensePlate, coordinates: Coordinates)
}
