package com.marley.parking.application.usecase

import com.marley.parking.domain.model.vo.Coordinates
import com.marley.parking.domain.model.vo.LicensePlate
import com.marley.parking.domain.pipeline.Pipeline
import com.marley.parking.domain.pipeline.handler.PipelineHandler
import com.marley.parking.domain.pipeline.parked.ParkedContext
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class VehicleParkedUseCaseTest : BehaviorSpec({

    val fixedInstant = Instant.parse("2026-06-15T14:30:00Z")
    val fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC)

    Given("um Clock fixo injetado no use case") {
        var capturedContext: ParkedContext? = null

        val capturingHandler = object : PipelineHandler<ParkedContext> {
            override fun handle(context: ParkedContext, next: (ParkedContext) -> ParkedContext): ParkedContext {
                capturedContext = context
                return next(context)
            }
        }

        val useCase = VehicleParkedUseCaseImpl(Pipeline(listOf(capturingHandler)), fixedClock)

        When("executa o PARKED") {
            useCase.execute(
                LicensePlate("ABC-1234"),
                Coordinates(-23.5505, -46.6333)
            )

            Then("o parkedTime deve ser exatamente o instante do Clock") {
                capturedContext!!.parkedTime shouldBe fixedInstant
            }
        }
    }
})
