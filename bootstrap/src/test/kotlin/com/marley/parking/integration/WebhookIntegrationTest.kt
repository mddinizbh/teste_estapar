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
import io.micronaut.http.client.exceptions.HttpClientResponseException
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

    Given("ENTRY duplicado para a mesma placa") {
        Then("deve retornar HTTP 422 com DUPLICATE_ENTRY") {
            val plate = "DUP-0001"

            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to plate,
                    "entry_time" to "2026-01-02T10:00:00Z"
                )),
                String::class.java
            )

            try {
                client.toBlocking().exchange(
                    HttpRequest.POST("/webhook", mapOf(
                        "event_type" to "ENTRY",
                        "license_plate" to plate,
                        "entry_time" to "2026-01-02T10:05:00Z"
                    )),
                    Map::class.java
                )
                throw AssertionError("Expected HttpClientResponseException")
            } catch (e: HttpClientResponseException) {
                e.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
            }
        }
    }

    Given("PARKED em vaga já ocupada por outro veículo") {
        Then("deve retornar HTTP 422 com SPOT_OCCUPIED") {
            val plate1 = "OCC-0001"
            val plate2 = "OCC-0002"

            // Vehicle 1: ENTRY + PARKED at spot (-23.5505, -46.6333)
            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to plate1,
                    "entry_time" to "2026-01-03T10:00:00Z"
                )),
                String::class.java
            )
            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "PARKED",
                    "license_plate" to plate1,
                    "lat" to -23.5505,
                    "lng" to -46.6333
                )),
                String::class.java
            )

            // Vehicle 2: ENTRY, then try to PARKED at the same spot
            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to plate2,
                    "entry_time" to "2026-01-03T10:05:00Z"
                )),
                String::class.java
            )

            try {
                client.toBlocking().exchange(
                    HttpRequest.POST("/webhook", mapOf(
                        "event_type" to "PARKED",
                        "license_plate" to plate2,
                        "lat" to -23.5505,
                        "lng" to -46.6333
                    )),
                    Map::class.java
                )
                throw AssertionError("Expected HttpClientResponseException")
            } catch (e: HttpClientResponseException) {
                e.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
            }
        }
    }

    Given("EXIT sem sessão ativa") {
        Then("deve retornar HTTP 404 com VEHICLE_NOT_FOUND") {
            try {
                client.toBlocking().exchange(
                    HttpRequest.POST("/webhook", mapOf(
                        "event_type" to "EXIT",
                        "license_plate" to "NONE-0001",
                        "exit_time" to "2026-01-04T11:00:00Z"
                    )),
                    Map::class.java
                )
                throw AssertionError("Expected HttpClientResponseException")
            } catch (e: HttpClientResponseException) {
                e.status shouldBe HttpStatus.NOT_FOUND
            }
        }
    }
})
