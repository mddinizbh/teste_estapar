package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.exception.SectorFullException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.SectorRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CheckSectorCapacityHandler(
    private val sectorRepository: SectorRepository
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val sector = sectorRepository.findWithAvailableCapacity()
            ?: throw SectorFullException()

        val occupied = sectorRepository.countOccupiedSpots(sector.name)
        val rate = sector.occupancyRate(occupied)

        logger.info { "Sector assigned | sector=${sector.name.value}, occupancy=$rate" }

        return next(context.copy(sector = sector, occupancyRate = rate))
    }
}
