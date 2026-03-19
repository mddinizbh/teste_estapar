package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.exception.SectorFullException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import com.marley.parking.domain.port.outbound.SectorRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CheckSectorCapacityHandler(
    private val sectorRepository: SectorRepository,
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val sector = sectorRepository.findWithAvailableCapacity()
            ?: throw SectorFullException()

        val activeSessions = parkingSessionRepository.countActiveBySector(sector.name)
        if (sector.isFull(activeSessions)) throw SectorFullException("Sector ${sector.name.value} is full")

        val rate = sector.occupancyRate(activeSessions)

        logger.info { "Sector assigned | sector=${sector.name.value}, activeSessions=$activeSessions, occupancy=$rate" }

        return next(context.copy(sector = sector, occupancyRate = rate))
    }
}
