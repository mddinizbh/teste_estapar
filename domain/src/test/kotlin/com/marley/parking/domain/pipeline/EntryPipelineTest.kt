package com.marley.parking.domain.pipeline

import com.marley.parking.domain.exception.SectorFullException
import com.marley.parking.domain.model.ParkingSession
import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.*
import com.marley.parking.domain.pipeline.entry.*
import com.marley.parking.domain.port.outbound.ParkingSessionRepository
import com.marley.parking.domain.port.outbound.SectorRepository
import com.marley.parking.domain.port.outbound.SpotRepository
import com.marley.parking.domain.service.PricingService
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

class EntryPipelineTest : BehaviorSpec({

    val pricingService = PricingService()

    Given("todos os setores estão cheios") {
        val sectorRepo = object : SectorRepository {
            override fun findByName(name: SectorName): Sector? = null
            override fun findAll(): List<Sector> = emptyList()
            override fun save(sector: Sector): Sector = sector
            override fun saveAll(sectors: List<Sector>) {}
        }
        val sessionRepo = stubSessionRepository()

        val pipeline = Pipeline(listOf(
            CheckSectorCapacityHandler(sectorRepo, sessionRepo),
            CalculateDynamicPriceHandler(pricingService),
            CreateSessionHandler(sessionRepo)
        ))

        When("um veículo tenta entrar") {
            Then("deve lançar SectorFullException") {
                shouldThrow<SectorFullException> {
                    pipeline.execute(EntryContext(
                        licensePlate = LicensePlate("ABC-1234"),
                        entryTime = Instant.parse("2026-01-01T10:00:00Z")
                    ))
                }
            }
        }
    }

    Given("um setor com vagas disponíveis") {
        val sector = Sector(
            id = 1L,
            name = SectorName("A"),
            basePrice = Money(BigDecimal("10.00")),
            maxCapacity = 10
        )

        val sectorRepo = object : SectorRepository {
            override fun findByName(name: SectorName): Sector? = sector
            override fun findAll(): List<Sector> = listOf(sector)
            override fun save(sector: Sector): Sector = sector
            override fun saveAll(sectors: List<Sector>) {}
        }
        val sessionRepo = stubSessionRepository(activeCount = 2)

        val pipeline = Pipeline(listOf(
            CheckSectorCapacityHandler(sectorRepo, sessionRepo),
            CalculateDynamicPriceHandler(pricingService),
            CreateSessionHandler(sessionRepo)
        ))

        When("um veículo entra") {
            val result = pipeline.execute(EntryContext(
                licensePlate = LicensePlate("ABC-1234"),
                entryTime = Instant.parse("2026-01-01T10:00:00Z")
            ))

            Then("deve criar session com preço dinâmico correto") {
                result.session shouldNotBe null
                result.priceAtEntry shouldBe Money(BigDecimal("9.00")) // 20% occupancy -> -10%
            }
        }
    }

    Given("um setor com 60% de ocupação") {
        val sector = Sector(
            id = 1L,
            name = SectorName("A"),
            basePrice = Money(BigDecimal("10.00")),
            maxCapacity = 10
        )

        val sectorRepo = object : SectorRepository {
            override fun findByName(name: SectorName): Sector? = sector
            override fun findAll(): List<Sector> = listOf(sector)
            override fun save(sector: Sector): Sector = sector
            override fun saveAll(sectors: List<Sector>) {}
        }
        val sessionRepo = stubSessionRepository(activeCount = 6)

        val pipeline = Pipeline(listOf(
            CheckSectorCapacityHandler(sectorRepo, sessionRepo),
            CalculateDynamicPriceHandler(pricingService),
            CreateSessionHandler(sessionRepo)
        ))

        When("um veículo entra") {
            val result = pipeline.execute(EntryContext(
                licensePlate = LicensePlate("ABC-1234"),
                entryTime = Instant.parse("2026-01-01T10:00:00Z")
            ))

            Then("o preço deve ter acréscimo de 10%") {
                result.priceAtEntry shouldBe Money(BigDecimal("11.00"))
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

private fun stubSessionRepository(activeCount: Int = 0) = object : ParkingSessionRepository {
    override fun save(session: ParkingSession): ParkingSession = session
    override fun findActiveByPlate(plate: LicensePlate): ParkingSession? = null
    override fun countActiveBySector(sectorName: SectorName): Int = activeCount
    override fun sumChargedBySectorAndDate(sectorName: SectorName, date: LocalDate): Money =
        Money(BigDecimal.ZERO)
}
