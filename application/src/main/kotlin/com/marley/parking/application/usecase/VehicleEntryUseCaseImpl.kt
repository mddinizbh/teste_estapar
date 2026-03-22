package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.entry.EntryContext
import com.marley.parking.domain.port.inbound.VehicleEntryUseCase
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.inject.Singleton
import jakarta.transaction.Transactional
import java.time.Instant

private val logger = KotlinLogging.logger {}

@Singleton
class VehicleEntryUseCaseImpl(
    private val entryPipeline: Pipeline<EntryContext>
) : VehicleEntryUseCase {

    @Transactional
    override fun execute(licensePlate: LicensePlate, entryTime: Instant) {
        logger.info { "Processando ENTRY | entryTime=$entryTime" }

        val context = EntryContext(
            licensePlate = licensePlate,
            entryTime = entryTime
        )
        val result = entryPipeline.execute(context)

        logger.info { "ENTRY processado | sector=${result.sector?.name?.value}, price=${result.priceAtEntry}" }
    }
}
