package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.exit.ExitContext
import com.marley.parking.domain.port.inbound.VehicleExitUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Singleton
class VehicleExitUseCaseImpl(
    private val exitPipeline: Pipeline<ExitContext>
) : VehicleExitUseCase {

    override fun execute(licensePlate: LicensePlate, exitTime: Instant) {
        logger.info { "Processing EXIT | plate=${licensePlate.value}, exitTime=$exitTime" }

        val context = ExitContext(
            licensePlate = licensePlate,
            exitTime = exitTime
        )
        val result = exitPipeline.execute(context)

        logger.info { "EXIT processed | plate=${licensePlate.value}, charged=${result.amountCharged}" }
    }
}
