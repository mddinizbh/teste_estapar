package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.exception.SectorFullException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.SpotRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class ReserveSpotHandler(
    private val spotRepository: SpotRepository
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val sector = context.sector!!
        val spot = spotRepository.findFirstAvailableBySector(sector.name)
            ?: throw SectorFullException("No available spots in sector ${sector.name.value}")

        spot.occupy()
        spotRepository.save(spot)

        logger.info { "Spot reserved | sector=${sector.name.value}, spotId=${spot.id}" }

        return next(context.copy(reservedSpot = spot))
    }
}
