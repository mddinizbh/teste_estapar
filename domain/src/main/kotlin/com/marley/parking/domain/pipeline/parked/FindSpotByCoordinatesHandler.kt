package com.marley.parking.domain.pipeline.parked

import com.marley.parking.domain.exception.VehicleNotFoundException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.SpotRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class FindSpotByCoordinatesHandler(
    private val spotRepository: SpotRepository
) : PipelineHandler<ParkedContext> {

    override fun handle(context: ParkedContext, next: (ParkedContext) -> ParkedContext): ParkedContext {
        val spot = spotRepository.findByCoordinates(context.coordinates)
            ?: throw VehicleNotFoundException("No spot found at coordinates (${context.coordinates.lat}, ${context.coordinates.lng})")

        logger.info { "Vaga encontrada | spotId=${spot.id}, coordenadas=${context.coordinates}" }

        return next(context.copy(spot = spot))
    }
}
