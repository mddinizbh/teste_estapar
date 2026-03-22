package com.marley.parking.domain.pipeline.exit

import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.SpotRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class ReleaseSpotHandler(
    private val spotRepository: SpotRepository
) : PipelineHandler<ExitContext> {

    override fun handle(context: ExitContext, next: (ExitContext) -> ExitContext): ExitContext {
        val session = context.session!!
        val spotId = session.spotId
        if (spotId == null) {
            logger.debug { "Sem vaga para liberar | plate=${context.licensePlate.value}" }
            return next(context)
        }

        val spot = spotRepository.findById(spotId)
        if (spot == null) {
            logger.debug { "Vaga não encontrada | spotId=$spotId" }
            return next(context)
        }

        spot.release()
        spotRepository.save(spot)

        logger.info { "Vaga liberada | spotId=$spotId" }

        return next(context.copy(spot = spot))
    }
}
