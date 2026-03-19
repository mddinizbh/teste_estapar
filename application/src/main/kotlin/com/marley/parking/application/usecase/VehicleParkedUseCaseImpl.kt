package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.parked.ParkedContext
import com.marley.parking.domain.port.inbound.VehicleParkedUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Singleton
class VehicleParkedUseCaseImpl(
    private val parkedPipeline: Pipeline<ParkedContext>
) : VehicleParkedUseCase {

    override fun execute(licensePlate: LicensePlate, coordinates: Coordinates) {
        logger.info { "Processing PARKED | plate=${licensePlate.value}, coordinates=$coordinates" }

        val context = ParkedContext(
            licensePlate = licensePlate,
            coordinates = coordinates,
            parkedTime = Instant.now()
        )
        parkedPipeline.execute(context)

        logger.info { "PARKED processed | plate=${licensePlate.value}" }
    }
}
