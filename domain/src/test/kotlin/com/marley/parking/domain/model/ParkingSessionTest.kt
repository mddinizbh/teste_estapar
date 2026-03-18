package com.marley.parking.domain.model

import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Instant

class ParkingSessionTest : BehaviorSpec({

    Given("uma session com status ENTERED") {
        val session = ParkingSession.enter(
            licensePlate = LicensePlate("ABC-1234"),
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )

        When("park() é chamado") {
            session.park(1L, Instant.parse("2026-01-01T10:05:00Z"))

            Then("o status deve mudar para PARKED") {
                session.status shouldBe ParkingStatus.PARKED
                session.spotId shouldBe 1L
            }
        }
    }

    Given("uma session com status PARKED") {
        val session = ParkingSession.enter(
            licensePlate = LicensePlate("ABC-1234"),
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )
        session.park(1L, Instant.parse("2026-01-01T10:05:00Z"))

        When("exit() é chamado") {
            session.exit(Money(BigDecimal("10.00")), Instant.parse("2026-01-01T11:00:00Z"))

            Then("o status deve mudar para EXITED") {
                session.status shouldBe ParkingStatus.EXITED
                session.amountCharged shouldBe Money(BigDecimal("10.00"))
            }
        }
    }

    Given("uma session com status ENTERED tentando exit()") {
        val session = ParkingSession.enter(
            licensePlate = LicensePlate("ABC-1234"),
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )

        When("exit() é chamado sem park() antes") {
            Then("deve lançar IllegalStateException") {
                shouldThrow<IllegalStateException> {
                    session.exit(Money(BigDecimal("10.00")), Instant.parse("2026-01-01T11:00:00Z"))
                }
            }
        }
    }

    Given("uma session com status PARKED tentando park() novamente") {
        val session = ParkingSession.enter(
            licensePlate = LicensePlate("ABC-1234"),
            sectorName = SectorName("A"),
            priceAtEntry = Money(BigDecimal("10.00")),
            entryTime = Instant.parse("2026-01-01T10:00:00Z")
        )
        session.park(1L, Instant.parse("2026-01-01T10:05:00Z"))

        When("park() é chamado novamente") {
            Then("deve lançar IllegalStateException") {
                shouldThrow<IllegalStateException> {
                    session.park(2L, Instant.parse("2026-01-01T10:10:00Z"))
                }
            }
        }
    }
})
