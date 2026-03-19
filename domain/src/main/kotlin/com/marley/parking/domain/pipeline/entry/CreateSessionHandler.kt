package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

class CreateSessionHandler(
    private val parkingSessionRepository: ParkingSessionRepository
) : PipelineHandler<EntryContext> {

    override fun handle(context: EntryContext, next: (EntryContext) -> EntryContext): EntryContext {
        val session = ParkingSession.enter(
            licensePlate = context.licensePlate,
            sectorName = context.sector!!.name,
            priceAtEntry = context.priceAtEntry!!,
            entryTime = context.entryTime
        )

        val saved = parkingSessionRepository.save(session)

        logger.info { "Session created | plate=${context.licensePlate.value}, sector=${context.sector!!.name.value}, price=${context.priceAtEntry}" }

        return next(context.copy(session = saved))
    }
}
