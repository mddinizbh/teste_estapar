package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.entry.EntryContext
import com.marley.parking.domain.port.inbound.VehicleEntryUseCase
import java.time.Instant

class VehicleEntryUseCaseImpl(
    private val entryPipeline: Pipeline<EntryContext>
) : VehicleEntryUseCase {

    override fun execute(licensePlate: LicensePlate, entryTime: Instant) {
        val context = EntryContext(
            licensePlate = licensePlate,
            entryTime = entryTime
        )
        entryPipeline.execute(context)
    }
}
