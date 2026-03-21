package com.marley.parking.integration

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class RevenueIntegrationTest(
    @Client("/") private val client: HttpClient
) : BehaviorSpec({

    Given("revenue após fluxo completo ENTRY -> PARKED -> EXIT") {
        Then("deve retornar valor maior que 0") {
            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to "REV-1234",
                    "entry_time" to "2026-02-01T10:00:00Z"
                )),
                String::class.java
            )

            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "PARKED",
                    "license_plate" to "REV-1234",
                    "lat" to -23.5505,
                    "lng" to -46.6333
                )),
                String::class.java
            )

            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "EXIT",
                    "license_plate" to "REV-1234",
                    "exit_time" to "2026-02-01T12:00:00Z"
                )),
                String::class.java
            )

            val revenueResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/revenue?date=2026-02-01&sector=A"),
                Map::class.java
            )
            revenueResponse.status shouldBe HttpStatus.OK
            val amount = (revenueResponse.body()?.get("amount") as Number).toDouble()
            amount shouldBeGreaterThan 0.0
        }
    }

    Given("revenue sem sessões no dia") {
        Then("deve retornar valor 0") {
            val revenueResponse = client.toBlocking().exchange(
                HttpRequest.GET<Any>("/revenue?date=2099-12-31&sector=A"),
                Map::class.java
            )
            revenueResponse.status shouldBe HttpStatus.OK
            val amount = (revenueResponse.body()?.get("amount") as Number).toDouble()
            amount shouldBe 0.0
        }
    }
})
