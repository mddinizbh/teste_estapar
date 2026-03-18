package com.marley.parking.domain.service

import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.OccupancyRate
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.math.BigDecimal
import java.time.Instant

class PricingServiceBehaviorTest : BehaviorSpec({

    val pricingService = PricingService()

    Given("um setor com lotação abaixo de 25%") {
        val basePrice = Money(BigDecimal("10.00"))
        val occupancyRate = OccupancyRate(0.20)

        When("um veículo entra") {
            val price = pricingService.calculateEntryPrice(basePrice, occupancyRate)

            Then("o preço deve ter desconto de 10%") {
                price shouldBe Money(BigDecimal("9.00"))
            }
        }
    }

    Given("um setor com lotação entre 25% e 50%") {
        val basePrice = Money(BigDecimal("10.00"))
        val occupancyRate = OccupancyRate(0.30)

        When("um veículo entra") {
            val price = pricingService.calculateEntryPrice(basePrice, occupancyRate)

            Then("o preço deve ser o preço base") {
                price shouldBe Money(BigDecimal("10.00"))
            }
        }
    }

    Given("um setor com lotação entre 50% e 75%") {
        val basePrice = Money(BigDecimal("10.00"))
        val occupancyRate = OccupancyRate(0.60)

        When("um veículo entra") {
            val price = pricingService.calculateEntryPrice(basePrice, occupancyRate)

            Then("o preço deve ter acréscimo de 10%") {
                price shouldBe Money(BigDecimal("11.00"))
            }
        }
    }

    Given("um setor com lotação entre 75% e 100%") {
        val basePrice = Money(BigDecimal("10.00"))
        val occupancyRate = OccupancyRate(0.80)

        When("um veículo entra") {
            val price = pricingService.calculateEntryPrice(basePrice, occupancyRate)

            Then("o preço deve ter acréscimo de 25%") {
                price shouldBe Money(BigDecimal("12.50"))
            }
        }
    }

    Given("cálculo de cobrança") {
        val priceAtEntry = Money(BigDecimal("10.00"))

        When("permanência é de 30 minutos ou menos") {
            val entryTime = Instant.parse("2026-01-01T10:00:00Z")
            val exitTime = Instant.parse("2026-01-01T10:30:00Z")
            val charge = pricingService.calculateCharge(priceAtEntry, entryTime, exitTime)

            Then("a cobrança deve ser gratuita") {
                charge shouldBe Money(BigDecimal.ZERO)
            }
        }

        When("permanência é exatamente 30 minutos") {
            val entryTime = Instant.parse("2026-01-01T10:00:00Z")
            val exitTime = Instant.parse("2026-01-01T10:30:00Z")
            val charge = pricingService.calculateCharge(priceAtEntry, entryTime, exitTime)

            Then("a cobrança deve ser gratuita") {
                charge shouldBe Money(BigDecimal.ZERO)
            }
        }

        When("permanência é de 45 minutos") {
            val entryTime = Instant.parse("2026-01-01T10:00:00Z")
            val exitTime = Instant.parse("2026-01-01T10:45:00Z")
            val charge = pricingService.calculateCharge(priceAtEntry, entryTime, exitTime)

            Then("deve cobrar 1 hora") {
                charge shouldBe Money(BigDecimal("10.00"))
            }
        }

        When("permanência é de 2h15min") {
            val entryTime = Instant.parse("2026-01-01T10:00:00Z")
            val exitTime = Instant.parse("2026-01-01T12:15:00Z")
            val charge = pricingService.calculateCharge(priceAtEntry, entryTime, exitTime)

            Then("deve cobrar 3 horas") {
                charge shouldBe Money(BigDecimal("30.00"))
            }
        }

        When("permanência é exatamente 1 hora") {
            val entryTime = Instant.parse("2026-01-01T10:00:00Z")
            val exitTime = Instant.parse("2026-01-01T11:00:00Z")
            val charge = pricingService.calculateCharge(priceAtEntry, entryTime, exitTime)

            Then("deve cobrar 1 hora") {
                charge shouldBe Money(BigDecimal("10.00"))
            }
        }
    }
})
