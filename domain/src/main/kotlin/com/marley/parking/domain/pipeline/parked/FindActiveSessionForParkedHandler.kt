package com.marley.parking.domain.pipeline.parked

import com.marley.parking.domain.exception.VehicleNotFoundException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class FindActiveSessionForParkedHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<ParkedContext> {

    override fun handle(context: ParkedContext, next: (ParkedContext) -> ParkedContext): ParkedContext {
        val session = parkingSessionRepository.findActiveByPlate(context.licensePlate)
            ?: throw VehicleNotFoundException("No active session for plate ${context.licensePlate.value}")

        logger.info { "Active session found for PARKED | plate=${context.licensePlate.value}" }

        return next(context.copy(session = session))
    }
}
