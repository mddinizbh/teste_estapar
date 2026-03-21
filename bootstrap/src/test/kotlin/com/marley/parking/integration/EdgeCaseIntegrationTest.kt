package com.marley.parking.integration

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class EdgeCaseIntegrationTest(
    @Client("/") private val client: HttpClient
) : BehaviorSpec({

    Given("EXIT sem PARKED (apenas ENTRY feito)") {
        Then("deve retornar HTTP 422 INVALID_SESSION_STATE") {
            val plate = "EDG-1234"

            client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to plate,
                    "entry_time" to "2026-03-01T10:00:00Z"
                )),
                String::class.java
            )

            try {
                client.toBlocking().exchange(
                    HttpRequest.POST("/webhook", mapOf(
                        "event_type" to "EXIT",
                        "license_plate" to plate,
                        "exit_time" to "2026-03-01T11:00:00Z"
                    )),
                    Map::class.java
                )
                throw AssertionError("Expected HttpClientResponseException")
            } catch (e: HttpClientResponseException) {
                e.status shouldBe HttpStatus.UNPROCESSABLE_ENTITY
            }
        }
    }

    Given("placa com formato inválido") {
        Then("deve retornar HTTP 400") {
            try {
                client.toBlocking().exchange(
                    HttpRequest.POST("/webhook", mapOf(
                        "event_type" to "ENTRY",
                        "license_plate" to "INVALID",
                        "entry_time" to "2026-03-01T10:00:00Z"
                    )),
                    Map::class.java
                )
                throw AssertionError("Expected HttpClientResponseException")
            } catch (e: HttpClientResponseException) {
                e.status shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }

    Given("event_type em branco") {
        Then("deve retornar HTTP 400") {
            try {
                client.toBlocking().exchange(
                    HttpRequest.POST("/webhook", mapOf(
                        "event_type" to "",
                        "license_plate" to "ABC-1234"
                    )),
                    Map::class.java
                )
                throw AssertionError("Expected HttpClientResponseException")
            } catch (e: HttpClientResponseException) {
                e.status shouldBe HttpStatus.BAD_REQUEST
            }
        }
    }
})
