package com.marley.parking.application.usecase

import com.marley.parking.domain.exception.VehicleNotFoundException
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.port.inbound.VehicleParkedUseCase
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Singleton
class VehicleParkedUseCaseImpl(
    private val parkingSessionRepository: ParkingSessionRepository,
    private val spotRepository: SpotRepository
) : VehicleParkedUseCase {

    override fun execute(licensePlate: LicensePlate, lat: Double, lng: Double) {
        logger.info { "Processing PARKED | plate=${licensePlate.value}, lat=$lat, lng=$lng" }

        val session = parkingSessionRepository.findActiveByPlate(licensePlate)
            ?: throw VehicleNotFoundException("No active session for plate ${licensePlate.value}")

        val spot = spotRepository.findByCoordinates(Coordinates(lat, lng))
            ?: throw VehicleNotFoundException("No spot found at coordinates ($lat, $lng)")

        session.park(spot.id, Instant.now())
        spotRepository.save(spot)
        parkingSessionRepository.save(session)

        logger.info { "PARKED processed | plate=${licensePlate.value}, spotId=${spot.id}" }
    }
}
