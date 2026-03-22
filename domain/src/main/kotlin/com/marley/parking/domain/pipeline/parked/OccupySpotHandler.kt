package com.marley.parking.domain.pipeline.parked

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.SpotRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class OccupySpotHandler(
    private val spotRepository: SpotRepository
) : PipelineHandler<ParkedContext> {

    override fun handle(context: ParkedContext, next: (ParkedContext) -> ParkedContext): ParkedContext {
        val spot = context.spot!!
        spot.occupy()
        spotRepository.save(spot)

        logger.info { "Vaga ocupada | spotId=${spot.id}" }

        return next(context)
    }
}
