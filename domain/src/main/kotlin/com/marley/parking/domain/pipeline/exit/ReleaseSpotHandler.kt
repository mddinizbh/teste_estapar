package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.SpotRepository

class ReleaseSpotHandler(
    private val spotRepository: SpotRepository
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        val spotId = session.spotId ?: return next(context)

        val spot = spotRepository.findById(spotId) ?: return next(context)
        spot.release()
        spotRepository.save(spot)

        return next(context.copy(spot = spot))
    }
}
