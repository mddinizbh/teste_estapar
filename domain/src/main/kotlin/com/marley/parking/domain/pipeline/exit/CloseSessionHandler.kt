package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository

class CloseSessionHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        session.exit(context.amountCharged!!, context.exitTime)

        val saved = parkingSessionRepository.save(session)
        return next(context.copy(session = saved))
    }
}
