package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.parked.ParkedContext
import com.marley.parking.domain.port.inbound.VehicleParkedUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.time.Clock
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Singleton
class VehicleParkedUseCaseImpl(
    private val parkedPipeline: Pipeline<ParkedContext>,
    private val clock: Clock
) : VehicleParkedUseCase {

    @Transactional
    override fun execute(licensePlate: LicensePlate, coordinates: Coordinates) {
        logger.info { "Processando PARKED | coordinates=$coordinates" }

        val context = ParkedContext(
            licensePlate = licensePlate,
            coordinates = coordinates,
            parkedTime = Instant.now(clock)
        )
        parkedPipeline.execute(context)

        logger.info { "PARKED processado" }
    }
}
