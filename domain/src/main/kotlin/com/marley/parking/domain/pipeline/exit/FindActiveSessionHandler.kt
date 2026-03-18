package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.exception.VehicleNotFoundException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository

class FindActiveSessionHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = parkingSessionRepository.findActiveByPlate(context.licensePlate)
            ?: throw VehicleNotFoundException("No active session for plate ${context.licensePlate.value}")

        return next(context.copy(session = session))
    }
}
