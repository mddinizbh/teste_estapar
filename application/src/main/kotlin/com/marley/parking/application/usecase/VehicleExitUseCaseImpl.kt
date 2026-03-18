package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.exit.ExitContext
import com.marley.parking.domain.port.inbound.VehicleExitUseCase
import java.time.Instant

class VehicleExitUseCaseImpl(
    private val exitPipeline: Pipeline<ExitContext>
) : VehicleExitUseCase {

    override fun execute(licensePlate: LicensePlate, exitTime: Instant) {
        val context = ExitContext(
            licensePlate = licensePlate,
            exitTime = exitTime
        )
        exitPipeline.execute(context)
    }
}
