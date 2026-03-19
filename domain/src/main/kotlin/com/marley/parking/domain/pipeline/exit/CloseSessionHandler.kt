package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CloseSessionHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        session.exit(context.amountCharged!!, context.exitTime)

        val saved = parkingSessionRepository.save(session)

        logger.info { "Session closed | plate=${context.licensePlate.value}, charged=${context.amountCharged}" }

        return next(context.copy(session = saved))
    }
}
