package com.marley.parking.integration

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.kotest5.annotation.MicronautTest

@MicronautTest
class WebhookIntegrationTest(
    @Client("/") private val client: HttpClient
) : BehaviorSpec({

    Given("um evento de ENTRY via webhook") {
        When("o evento é enviado para /webhook") {
            val response = client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "ENTRY",
                    "license_plate" to "ABC-1234",
                    "entry_time" to "2026-01-01T10:00:00Z"
                )),
                String::class.java
            )

            Then("retorna HTTP 200") {
                response.status shouldBe HttpStatus.OK
            }
        }
    }

    Given("um evento com tipo desconhecido") {
        When("o evento é enviado para /webhook") {
            val response = client.toBlocking().exchange(
                HttpRequest.POST("/webhook", mapOf(
                    "event_type" to "UNKNOWN"
                )),
                String::class.java
            )

            Then("retorna HTTP 200 mesmo assim") {
                response.status shouldBe HttpStatus.OK
            }
        }
    }
})
