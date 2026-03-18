package com.marley.parking.domain.port.inbound

import com.marley.parking.domain.model.vo.LicensePlate

interface VehicleParkedUseCase {
    fun execute(licensePlate: LicensePlate, lat: Double, lng: Double)
}
