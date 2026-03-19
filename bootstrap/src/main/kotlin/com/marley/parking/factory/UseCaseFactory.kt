package com.marley.parking.factory

import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.entry.*
import com.marley.parking.domain.pipeline.exit.*
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import com.marley.parking.domain.port.outbound.SectorRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import com.marley.parking.domain.service.PricingService
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class UseCaseFactory {

    @Singleton
    fun pricingService(): PricingService = PricingService()

    @Singleton
    fun entryPipeline(
        sectorRepository: SectorRepository,
        spotRepository: SpotRepository,
        parkingSessionRepository: ParkingSessionRepository,
        pricingService: PricingService
    ): Pipeline<EntryContext> = Pipeline(
        listOf(
            CheckDuplicateEntryHandler(parkingSessionRepository),
            CheckSectorCapacityHandler(sectorRepository, parkingSessionRepository),
            CalculateDynamicPriceHandler(pricingService),
            CreateSessionHandler(parkingSessionRepository)
        )
    )

    @Singleton
    fun exitPipeline(
        parkingSessionRepository: ParkingSessionRepository,
        spotRepository: SpotRepository,
        pricingService: PricingService
    ): Pipeline<ExitContext> = Pipeline(
        listOf(
            FindActiveSessionHandler(parkingSessionRepository),
            CalculateChargeHandler(pricingService),
            ReleaseSpotHandler(spotRepository),
            CloseSessionHandler(parkingSessionRepository)
        )
    )
}
