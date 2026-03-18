package com.marley.parking.factory

import com.marley.parking.application.usecase.RevenueQueryUseCaseImpl
import com.marley.parking.application.usecase.VehicleEntryUseCaseImpl
import com.marley.parking.application.usecase.VehicleExitUseCaseImpl
import com.marley.parking.application.usecase.VehicleParkedUseCaseImpl
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.entry.*
import com.marley.parking.domain.pipeline.exit.*
import com.marley.parking.domain.port.inbound.RevenueQueryUseCase
import com.marley.parking.domain.port.inbound.VehicleEntryUseCase
import com.marley.parking.domain.port.inbound.VehicleExitUseCase
import com.marley.parking.domain.port.inbound.VehicleParkedUseCase
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
            CheckSectorCapacityHandler(sectorRepository),
            CalculateDynamicPriceHandler(pricingService),
            ReserveSpotHandler(spotRepository),
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

    @Singleton
    fun vehicleEntryUseCase(
        entryPipeline: Pipeline<EntryContext>
    ): VehicleEntryUseCase = VehicleEntryUseCaseImpl(entryPipeline)

    @Singleton
    fun vehicleParkedUseCase(
        parkingSessionRepository: ParkingSessionRepository,
        spotRepository: SpotRepository
    ): VehicleParkedUseCase = VehicleParkedUseCaseImpl(parkingSessionRepository, spotRepository)

    @Singleton
    fun vehicleExitUseCase(
        exitPipeline: Pipeline<ExitContext>
    ): VehicleExitUseCase = VehicleExitUseCaseImpl(exitPipeline)

    @Singleton
    fun revenueQueryUseCase(
        parkingSessionRepository: ParkingSessionRepository
    ): RevenueQueryUseCase = RevenueQueryUseCaseImpl(parkingSessionRepository)
}
