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
        val sectors = sectorRepository.findAll()
        var activeCount = 0
        val sector = sectors.firstOrNull { s ->
            activeCount = parkingSessionRepository.countActiveBySector(s.name)
            !s.isFull(activeCount)
        } ?: throw SectorFullException()

        val rate = sector.occupancyRate(activeCount)

        logger.info { "Sector assigned | sector=${sector.name.value}, activeSessions=$activeCount, occupancy=$rate" }

        return next(context.copy(sector = sector, occupancyRate = rate))
    }
}
