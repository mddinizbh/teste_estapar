package com.marley.parking.domain.pipeline.entry

import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.port.outbound.ParkingSessionRepository

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
        return next(context.copy(session = saved))
    }
}
