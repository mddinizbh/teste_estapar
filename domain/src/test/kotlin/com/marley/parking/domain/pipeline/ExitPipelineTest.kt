package com.marley.parking.domain.pipeline

import com.marley.parking.domain.exception.VehicleNotFoundException
import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.*
import com.marley.parking.domain.pipeline.exit.*
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import com.marley.parking.domain.service.PricingService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class ExitPipelineTest : BehaviorSpec({

    val pricingService = PricingService()

    Given("um veículo que não está no estacionamento") {
        val sessionRepo = object : ParkingSessionRepository {
            override fun save(session: ParkingSession): ParkingSession = session
            override fun findActiveByPlate(plate: LicensePlate): ParkingSession? = null
            override fun sumChargedBySectorAndDate(sectorName: SectorName, date: LocalDate): Money =
                Money(BigDecimal.ZERO)
        }
        val spotRepo = stubSpotRepository()

        val pipeline = Pipeline(listOf(
            FindActiveSessionHandler(sessionRepo),
            CalculateChargeHandler(pricingService),
            ReleaseSpotHandler(spotRepo),
            CloseSessionHandler(sessionRepo)
        ))

        When("tenta sair") {
            Then("deve lançar VehicleNotFoundException") {
                shouldThrow<VehicleNotFoundException> {
                    pipeline.execute(ExitContext(
                        licensePlate = LicensePlate("XYZ-9999"),
                        exitTime = Instant.parse("2026-01-01T12:00:00Z")
                    ))
                }
            }
        }
    }

    Given("um veículo estacionado por 45 minutos") {
        val session = ParkingSession.enter(
            licensePlate = LicensePlate("ABC-1234"),
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )
        session.park(1L, Instant.parse("2026-01-01T10:05:00Z"))

        val spot = Spot(1L, SectorName("A"), Coordinates(-23.5, -46.6), occupied = true)

        val sessionRepo = object : ParkingSessionRepository {
            override fun save(session: ParkingSession): ParkingSession = session
            override fun findActiveByPlate(plate: LicensePlate): ParkingSession? = session
            override fun sumChargedBySectorAndDate(sectorName: SectorName, date: LocalDate): Money =
                Money(BigDecimal.ZERO)
        }
        val spotRepo = object : SpotRepository {
            override fun findById(id: Long): Spot? = spot
            override fun findByCoordinates(coordinates: Coordinates): Spot? = spot
            override fun findFirstAvailableBySector(sectorName: SectorName): Spot? = null
            override fun save(spot: Spot): Spot = spot
            override fun saveAll(spots: List<Spot>) {}
        }

        val pipeline = Pipeline(listOf(
            FindActiveSessionHandler(sessionRepo),
            CalculateChargeHandler(pricingService),
            ReleaseSpotHandler(spotRepo),
            CloseSessionHandler(sessionRepo)
        ))

        When("o veículo sai") {
            val result = pipeline.execute(ExitContext(
                licensePlate = LicensePlate("ABC-1234"),
                exitTime = Instant.parse("2026-01-01T10:45:00Z")
            ))

            Then("deve calcular cobrança de 1 hora e liberar a vaga") {
                result.amountCharged shouldBe Money(BigDecimal("10.00"))
                spot.isOccupied shouldBe false
            }
        }
    }
})

private fun stubSpotRepository() = object : SpotRepository {
    override fun findById(id: Long): Spot? = null
    override fun findByCoordinates(coordinates: Coordinates): Spot? = null
    override fun findFirstAvailableBySector(sectorName: SectorName): Spot? = null
    override fun save(spot: Spot): Spot = spot
    override fun saveAll(spots: List<Spot>) {}
}
