package com.marley.parking.domain.pipeline.parked

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class ParkSessionHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<ParkedContext> {

    override fun handle(context: ParkedContext, next: (ParkedContext) -> ParkedContext): ParkedContext {
        val session = context.session!!
        val spot = context.spot!!

        session.park(spot.id, context.parkedTime)
        val saved = parkingSessionRepository.save(session)

        logger.info { "Sessão estacionada | plate=${context.licensePlate.value}, spotId=${spot.id}" }

        return next(context.copy(session = saved))
    }
}
