package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.exception.DuplicateEntryException
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CheckDuplicateEntryHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val existing = parkingSessionRepository.findActiveByPlate(context.licensePlate)
        if (existing != null) {
            throw DuplicateEntryException("Vehicle ${context.licensePlate.value} already has an active session")
        }

        logger.info { "No duplicate entry | plate=${context.licensePlate.value}" }

        return next(context)
    }
}
