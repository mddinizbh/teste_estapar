package com.marley.parking.domain.pipeline

import com.marley.parking.domain.exception.SpotAlreadyOccupiedException
import com.marley.parking.domain.exception.VehicleNotFoundException
import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.ParkingStatus
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.*
import com.marley.parking.domain.pipeline.parked.*
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class ParkedPipelineTest : BehaviorSpec({

    val defaultCoordinates = Coordinates(-23.5, -46.6)
    val defaultPlate = LicensePlate("ABC-1234")
    val parkedTime = Instant.parse("2026-01-01T10:05:00Z")

    Given("sem sessão ativa para a placa") {
        val sessionRepo = stubSessionRepository(activeSession = null)
        val spotRepo = stubSpotRepository(spot = null)

        val pipeline = buildParkedPipeline(sessionRepo, spotRepo)

        When("recebe evento PARKED") {
            Then("deve lançar VehicleNotFoundException") {
                shouldThrow<VehicleNotFoundException> {
                    pipeline.execute(ParkedContext(
                        licensePlate = defaultPlate,
                        coordinates = defaultCoordinates,
                        parkedTime = parkedTime
                    ))
                }
            }
        }
    }

    Given("sessão ativa mas coordenadas sem vaga") {
        val session = ParkingSession.enter(
            licensePlate = defaultPlate,
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )
        val sessionRepo = stubSessionRepository(activeSession = session)
        val spotRepo = stubSpotRepository(spot = null)

        val pipeline = buildParkedPipeline(sessionRepo, spotRepo)

        When("recebe evento PARKED") {
            Then("deve lançar VehicleNotFoundException") {
                shouldThrow<VehicleNotFoundException> {
                    pipeline.execute(ParkedContext(
                        licensePlate = defaultPlate,
                        coordinates = defaultCoordinates,
                        parkedTime = parkedTime
                    ))
                }
            }
        }
    }

    Given("sessão ativa e vaga já ocupada") {
        val session = ParkingSession.enter(
            licensePlate = defaultPlate,
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )
        val spot = Spot(1L, SectorName("A"), defaultCoordinates, occupied = true)

        val sessionRepo = stubSessionRepository(activeSession = session)
        val spotRepo = stubSpotRepository(spot = spot)

        val pipeline = buildParkedPipeline(sessionRepo, spotRepo)

        When("recebe evento PARKED") {
            Then("deve lançar SpotAlreadyOccupiedException") {
                shouldThrow<SpotAlreadyOccupiedException> {
                    pipeline.execute(ParkedContext(
                        licensePlate = defaultPlate,
                        coordinates = defaultCoordinates,
                        parkedTime = parkedTime
                    ))
                }
            }
        }
    }

    Given("sessão ativa e vaga disponível") {
        val session = ParkingSession.enter(
            licensePlate = defaultPlate,
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )
        val spot = Spot(1L, SectorName("A"), defaultCoordinates, occupied = false)

        val sessionRepo = stubSessionRepository(activeSession = session)
        val spotRepo = stubSpotRepository(spot = spot)

        val pipeline = buildParkedPipeline(sessionRepo, spotRepo)

        When("recebe evento PARKED") {
            val result = pipeline.execute(ParkedContext(
                licensePlate = defaultPlate,
                coordinates = defaultCoordinates,
                parkedTime = parkedTime
            ))

            Then("session deve ficar PARKED e spot ocupado") {
                result.session!!.status shouldBe ParkingStatus.PARKED
                result.session!!.spotId shouldBe 1L
                spot.isOccupied shouldBe true
            }
        }
    }
})

private fun buildParkedPipeline(
    sessionRepo: ParkingSessionRepository,
    spotRepo: SpotRepository
): Pipeline<ParkedContext> = Pipeline(
    listOf(
        FindActiveSessionForParkedHandler(sessionRepo),
        FindSpotByCoordinatesHandler(spotRepo),
        OccupySpotHandler(spotRepo),
        ParkSessionHandler(sessionRepo)
    )
)

private fun stubSessionRepository(activeSession: ParkingSession?) = object : ParkingSessionRepository {
    override fun save(session: ParkingSession): ParkingSession = session
    override fun findActiveByPlate(plate: LicensePlate): ParkingSession? = activeSession
    override fun countActiveBySector(sectorName: SectorName): Int = 0
    override fun sumChargedBySectorAndDate(sectorName: SectorName, date: LocalDate): Money =
        Money(BigDecimal.ZERO)
}

private fun stubSpotRepository(spot: Spot?) = object : SpotRepository {
    override fun findById(id: Long): Spot? = spot
    override fun findByCoordinates(coordinates: Coordinates): Spot? = spot
    override fun findFirstAvailableBySector(sectorName: SectorName): Spot? = null
    override fun save(spot: Spot): Spot = spot
    override fun saveAll(spots: List<Spot>) {}
}
