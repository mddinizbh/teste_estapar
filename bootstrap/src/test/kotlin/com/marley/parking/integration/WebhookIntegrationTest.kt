package com.marley.parking.integration

import com.marley.parking.domain.model.Sector
import com.marley.parking.domain.model.Spot
import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.Money
import com.marley.parking.domain.model.vo.SectorName
import com.marley.parking.domain.port.outbound.GarageConfig
import com.marley.parking.domain.port.outbound.GarageConfigLoader
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.micronaut.context.annotation.Replaces
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest
import jakarta.inject.Singleton
import java.math.BigDecimal

@Singleton
@Replaces(GarageConfigLoader::class)
class TestGarageConfigLoader : GarageConfigLoader {
    override fun loadConfig(): GarageConfig {
        return GarageConfig(
            sectors = listOf(
                Sector(
                    name = SectorName("A"),
                    basePrice = Money(BigDecimal("10.00")),
                    maxCapacity = 100
                )
            ),
            spots = listOf(
                Spot(
                    id = 1L,
                    sectorName = SectorName("A"),
                    coordinates = Coordinates(-23.5505, -46.6333)
                ),
                Spot(
                    id = 2L,
                    sectorName = SectorName("A"),
                    coordinates = Coordinates(-23.5506, -46.6334)
                )
            )
        )
    }
}

@MicronautTest
class WebhookIntegrationTest(
    @Client("/") private val client: HttpClient
) : BehaviorSpec({

    Given("fluxo completo: ENTRY -> PARKED -> EXIT -> revenue") {
        Then("deve processar ENTRY, PARKED, EXIT e retornar revenue correto") {
            val entryResponse = client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to "ABC-1234",
                    "entry_time" to "2026-01-01T10:00:00Z"
                )),
                String::class.java
            )
            entryResponse.status shouldBe HttpStatus.OK

            val parkedResponse = client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "PARKED",
                    "license_plate" to "ABC-1234",
                    "lat" to -23.5505,
                    "lng" to -46.6333
                )),
                String::class.java
            )
            parkedResponse.status shouldBe HttpStatus.OK

            val exitResponse = client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "EXIT",
                    "license_plate" to "ABC-1234",
                    "exit_time" to "2026-01-01T11:00:00Z"
                )),
                String::class.java
            )
            exitResponse.status shouldBe HttpStatus.OK

            val revenueResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/revenue?date=2026-01-01&sector=A"),
                Map::class.java
            )
            revenueResponse.status shouldBe HttpStatus.OK
            val amount = (revenueResponse.body()?.get("amount") as Number).toDouble()
            amount shouldBeGreaterThan 0.0
        }
    }

    Given("um evento com tipo desconhecido") {
        Then("retorna HTTP 200 mesmo assim") {
            val response = client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "UNKNOWN"
                )),
                String::class.java
            )
            response.status shouldBe HttpStatus.OK
        }
    }
})
